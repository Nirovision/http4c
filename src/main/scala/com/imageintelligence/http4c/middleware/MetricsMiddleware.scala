package com.imageintelligence.http4c.middleware

import cats.effect._
import cats.implicits._
import cats._
import org.http4s._
import scala.concurrent.duration._

object MetricsMiddleware {

  val metricsPrefix = "http4s"

  def apply[F[_]](
    increment: String => Unit,
    histogram: (String, Long, String*) => Unit,
    servicePrefix: String)(implicit F: Effect[F]
  ): HttpMiddleware[F] = { service =>

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

    def onFinish(method: Method, start: Long)(responseE: Either[Throwable, MaybeResponse[F]]): Either[Throwable, MaybeResponse[F]] = {
      val elapsed = (System.nanoTime() - start).nanos

      responseE.map { r =>
        histogram(prefix("headers.ms"), (System.nanoTime() - start).nanos.toMillis)
        r match {
          case a: Response[F] => {
            val code = a.status.code
            val x: fs2.Stream[F, Byte] = a.body
              .onFinalize {
                F.delay{
                  generalMetrics(method, code, elapsed)
                }
              }
              .onError { cause =>
                histogram(prefix("abnormal_termination.ms"), elapsed.toMillis)
                fs2.Stream.fail(cause)
              }
            a.copy(body = x)
          }
          case other => other
        }
      }
    }

    Service.lift { req: Request[F] =>
      val now = System.nanoTime()
      increment(prefix("active_requests"))
      service(req).attempt.flatMap(onFinish(req.method, now)(_).fold(F.raiseError, F.pure))
    }
  }
}
