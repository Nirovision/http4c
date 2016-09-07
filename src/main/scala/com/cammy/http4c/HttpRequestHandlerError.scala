package com.cammy.http4c

import org.http4s.{Response, Request}

import scalaz._, Scalaz._

case class HttpRequestHandlerError[E](clientResponse: Response, internalError: Option[E]) {
  def fold[A](handler: Response => Option[E] => A): A =
    handler(clientResponse)(internalError)
}
