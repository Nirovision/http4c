package com.imageintelligence.http4c.middleware

import org.http4s._
import com.imageintelligence.http4c.headers.`X-Http-Method-Override`
import cats._
import cats.implicits._

object XHttpMethodOverrideMiddleware {
  def defaultFailureResponse[F[_]: Monad]: F[Response[F]] =
    Response[F](Status.BadRequest).withBody(s"Invalid HTTP method provided in ${`X-Http-Method-Override`.name} header")

  def apply[F[_]: Monad](service: HttpService[F], failureResponseO: Option[F[Response[F]]] = None): HttpService[F] = HttpService.lift[F] { req =>
    req.headers.get(`X-Http-Method-Override`) match {
      case Some(method) => Method.fromString(method.method) match {
        case Left(_) => {
          defaultFailureResponse[F].map(_.asMaybeResponse)
        }
        case Right(success) => {
          Method.registered.exists(_ == success) match {
            case false => {
              failureResponseO.fold(defaultFailureResponse[F])(response => response).map(_.asMaybeResponse)
            }
            case true  => service(req.copy(method = success))
          }
        }
      }
      case None => service(req)
    }
  }
}
