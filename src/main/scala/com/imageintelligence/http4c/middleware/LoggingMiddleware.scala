package com.imageintelligence.http4c.middleware

import org.http4s.server._
import argonaut._
import Argonaut._
import org.http4s._
import cats._
import cats.implicits._

case class JsonLogLine[F[_]: Applicative](req: Request[F], resp: MaybeResponse[F])

object JsonLogLine {
  implicit def EncodeJsonLogLine[F[_]: Applicative]: EncodeJson[JsonLogLine[F]] = EncodeJson { a =>
    Json(
      "request" := Json(
        "method" := a.req.method.toString,
        "uri" := a.req.uri.toString,
        "headers" := a.req.headers.toList.map(h => (h.name.toString, h.value)).toMap,
        "queryString" := a.req.queryString,
        "remoteUser" := a.req.remoteUser,
        "remoteHost" := a.req.remoteHost
      ),
      "response" := Json(
        "code" := a.resp.orNotFound.status.code.toString,
        "headers" := a.resp.orNotFound.headers.toList.map(h => (h.name.toString, h.value)).toMap
      )
    )
  }
}

object LoggingMiddleware {

  def toLog[F[_]: Applicative](req: Request[F]): Boolean = {
    val notHealth = req.pathInfo != "/health"
    val notStatus = req.pathInfo != "/status"
    val notOptions = req.method != Method.OPTIONS
    notHealth && notStatus && notOptions
  }

  def apply[F[_]: Applicative](shouldLog: Request[F] => Boolean)(log: (Request[F], MaybeResponse[F]) => Unit): HttpMiddleware[F] = { service =>
    HttpService.lift[F] { request =>
      service.run(request).map { response =>
        if (shouldLog(request)) {
          log(request, response)
        }
        response
      }
    }
  }

  def jsonLoggingMiddleware[F[_]: Applicative](log: String => Unit, shouldLog: Request[F] => Boolean): HttpMiddleware[F] = {
    apply(shouldLog) { case (req, resp) =>
      log(JsonLogLine(req, resp).asJson.nospaces)
    }
  }

  def basicLoggingMiddleware[F[_]: Applicative](log: String => Unit, shouldLog: Request[F] => Boolean): HttpMiddleware[F] = {
    def floorCode(code: Int): Int = {
      (code / 100) * 100
    }

    apply(shouldLog) { case (req, resp) =>
      val code = resp.orNotFound.status.code
      val defaultMsg = s"${resp.orNotFound.status.code} - ${req.method} ${req.uri.toString}"

      val msg = floorCode(code) match {
        case 100 => defaultMsg
        case 200 => defaultMsg
        case 300 => defaultMsg
        case 400 => s"${defaultMsg} - Req: ${req} - Resp: ${resp}"
        case 500 => s"${defaultMsg} - Req: ${req} - Resp: ${resp}"
      }
      log(msg)
    }
  }

}
