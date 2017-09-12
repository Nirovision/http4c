package com.imageintelligence.http4c

import java.util.concurrent.TimeUnit

import argonaut._
import Argonaut._
import org.http4s._
import org.http4s.dsl._
import com.imageintelligence.http4c.Health._
import ArgonautInstances._
import cats.implicits._
import cats._

import scala.concurrent.duration.Duration

object Health {

  case class UptimeReport(startedAt: Long) {
    def asMillis: Long =
      System.currentTimeMillis - startedAt

    def asHumanized: String = {
      val millis = asMillis
      val days = TimeUnit.MILLISECONDS.toDays(millis)
      val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
      val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
      val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
      s"$days days $hours hours $minutes minutes $seconds seconds"
    }
  }

  implicit def UptimeReportEncodeJson = EncodeJson[UptimeReport](
    a => Json(
      "humanized" := a.asHumanized,
      "ms"        := a.asMillis
    )
  )

  sealed trait HealthCheckStatus
  final case object HealthCheckSuccess extends HealthCheckStatus
  final case class HealthCheckFailure(reason: Throwable, fatal: Boolean) extends HealthCheckStatus

  case class HealthCheck[F[_]: Applicative](name: String, check: F[HealthCheckStatus])

  case class HealthCheckResult(name: String, status: HealthCheckStatus, timeTaken: Duration)

  case class HealthReportResult(uptime: UptimeReport, results: List[HealthCheckResult]) {

    def isFatal = results.exists { x =>
      x.status match {
        case HealthCheckFailure(_, fatal) => fatal
        case e => false
      }
    }
  }

  case class HealthReport[F[_]: Monad](checks: List[HealthCheck[F]]) {
    def check: F[HealthReportResult] = {
      val results: F[List[HealthCheckResult]] = checks.traverse { c =>
        val timed = timeM(System.currentTimeMillis().pure[F], c.check)
        timed.map { case (duration, status) => HealthCheckResult(c.name, status, duration) }
      }
      results.map(x => HealthReportResult(uptime, x))
    }

    val uptime: UptimeReport = UptimeReport(System.currentTimeMillis)
  }

  implicit def HealthCheckStatusEncodeJson = EncodeJson[HealthCheckStatus] { h =>
    h match {
      case HealthCheckSuccess =>
        Json("healthy" := true)
      case HealthCheckFailure(reason, fatal) =>
        Json("healthy" := false, "fatal" := fatal, "message" := reason.getMessage)
    }
  }

  implicit def HealthCheckResultEncodeJson = EncodeJson[HealthCheckResult] { h =>
    Json(
      h.name := h.status.asJson.deepmerge(Json("time" := s"${h.timeTaken.toMillis.toString}ms"))
    )
  }

  implicit def HealthReportResultEncodeJson = EncodeJson[HealthReportResult] { h =>
    Json (
      "checks" := h.results.foldLeft(jEmptyObject){ case (i, e) => i.deepmerge(e.asJson) },
      "uptime" := h.uptime
    )
  }

  private def timeM[M[_]: Monad, A](now: M[Long], task: M[A]): M[(Duration, A)] = {
    now.flatMap { start =>
      for {
        x <- task
        end <- now
      } yield (Duration(end - start, TimeUnit.MILLISECONDS), x)
    }
  }
}

case class HealthReportService[F[_]: Monad](healthReport: HealthReport[F]) {
  val service = HttpService[F] {
    case req @ GET -> Root => {
      healthReport.check.flatMap { result =>
        if (result.isFatal) {
          Response(Status.Ok).withBody(result.asJson)
        } else {
          Response(Status.Ok).withBody(result.asJson)
        }
      }
    }
  }
}
