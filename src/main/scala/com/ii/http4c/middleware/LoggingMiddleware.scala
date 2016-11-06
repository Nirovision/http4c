package com.ii.http4c.middleware
import org.http4s._

object LoggingMiddleware {

  def apply(service: HttpService)(log: (Request, Response) => Unit): HttpService = {
    HttpService.lift { request =>
      service.run(request).map { response =>
        log(request, response)
        response
      }
    }
  }

  def basicLoggingMiddleware(log: String => Unit): HttpService => HttpService = {
    apply(_) { case (req, resp) =>

      val code = resp.status.code
      val defaultMsg = s"${resp.status.code} - ${req.method} ${req.uri.toString}"

      val msg = floorCode(code) match {
        case 100 => defaultMsg
        case 200 => defaultMsg
        case 300 => defaultMsg
        case 400 => s"${defaultMsg} - Req: ${req} - Resp: ${resp}"
        case 500 => s"${defaultMsg} - Req: ${req} - Resp: ${resp}"
      }
      if (toLog(req)) {
        log(msg)
      }
    }
  }

  private def floorCode(code: Int): Int = {
    (code / 100) * 100
  }

  private def toLog(req: Request): Boolean = {
    val notHealth = req.pathInfo != "/health"
    val notStatus = req.pathInfo != "/status"
    val notOptions = req.method != Method.OPTIONS
    notHealth && notStatus && notOptions
  }

}
