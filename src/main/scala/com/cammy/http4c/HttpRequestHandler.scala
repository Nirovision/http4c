package com.cammy.http4c

import org.http4s.{Response, Request}

import scalaz._, Scalaz._

case class HttpRequestHandlerError[E](clientResponse: Response, internalError: Option[E]) {
  def fold[A](handler: Response => Option[E] => A): A =
    handler(clientResponse)(internalError)
}

case class HttpRequestHandler[M[_]: Monad, E](value: Request => EitherT[M, HttpRequestHandlerError[E], Response]) {

  def toHttp4s[A](internalErrorHandler: E => M[Unit]): Request => M[Response] = { req =>
    value(req).run.flatMap {
      case -\/(HttpRequestHandlerError(resp, me)) => me.cata(internalErrorHandler(_), ().pure[M]).map(_ => resp)
      case \/-(resp)                              => resp.pure[M]
    }
  }

}
