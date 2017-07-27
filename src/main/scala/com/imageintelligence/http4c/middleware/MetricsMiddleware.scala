package com.imageintelligence.http4c.middleware

import org.http4s._

import scala.concurrent.duration._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import scalaz.stream.Cause.End
import scalaz.stream.Process._

object MetricsMiddleware {

  val metricsPrefix = "http4s"

  def apply(increment: String => Unit, histogram: (String, Long, String*) => Unit, servicePrefix: String): HttpMiddleware = { service =>

    def prefix(str: String): String = {
      s"${metricsPrefix}.${servicePrefix}.${str}"
    }

    def statusCodeToTag(code: Int): String = {
      val flooredCode = (code / 100)
      s"status:code_${flooredCode.toString}xx"
    }

    def generalMetrics(method: Method, code: Int, elapsed: FiniteDuration): Unit = {
      val tag = statusCodeToTag(code)
      histogram(prefix(method.name.toLowerCase + ".response.ms"), elapsed.toMillis, tag)
      histogram(prefix("response.ms"), elapsed.toMillis, tag)
    }

    def onFinish(method: Method, start: Long)(responseE: Throwable \/ Response): Throwable \/ Response = {
      val elapsed = (System.nanoTime() - start).nanos
      responseE match {
        case \/-(response) => {
          histogram(prefix("headers.ms"), (System.nanoTime() - start).nanos.toMillis)
          val code = response.status.code
          val body = response.body.onHalt { cause =>
            generalMetrics(method, code, elapsed)
            cause match {
              case End => halt
              case _   =>
                histogram(prefix("abnormal_termination.ms"), elapsed.toMillis)
                Halt(cause)
            }
          }
          response.copy(body = body).right
        }
        case -\/(e) => {
          generalMetrics(method, 500, elapsed)
          histogram(prefix("service_failure.ms"), elapsed.toMillis)
          e.left
        }
      }
    }

    Service.lift { req: Request =>
      val now = System.nanoTime()
      increment(prefix("active_requests"))
      service(req).attempt.flatMap(onFinish(req.method, now)(_).fold(Task.fail, Task.now))
    }
  }
}
