package com.ii.http4c

import org.http4s.{Response, Request}

import scalaz._, Scalaz._

case class HttpRequestHandler[M[_]: Monad, E](value: Request => EitherT[M, HttpRequestHandlerError[E], Response]) {

  def toHttp4s[A](internalErrorHandler: E => M[Unit]): Request => M[Response] = { req =>
    value(req).run.flatMap {
      case -\/(HttpRequestHandlerError(resp, me)) => me.cata(internalErrorHandler(_), ().pure[M]).map(_ => resp)
      case \/-(resp)                              => resp.pure[M]
    }
  }

}
