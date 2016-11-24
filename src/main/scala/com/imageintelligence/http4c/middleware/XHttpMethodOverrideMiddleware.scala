package com.imageintelligence.http4c.middleware

import org.http4s._
import com.imageintelligence.http4c.headers.`X-Http-Method-Override`
import scalaz.concurrent.Task
import scalaz.{\/-, -\/}

object XHttpMethodOverrideMiddleware {
  def defaultFailureResponse: Task[Response] =
    Response(Status.BadRequest).withBody(s"Invalid HTTP method provided in ${`X-Http-Method-Override`.name} header")

  def apply(service: HttpService, failureResponseO: Option[Response] = None): HttpService = Service.lift { req =>
    req.headers.get(`X-Http-Method-Override`) match {
      case Some(method) => Method.fromString(method.method) match {
        case -\/(failure) => failureResponseO.fold(defaultFailureResponse)(response => Task(response))
        case \/-(success) => {
          Method.registered.exists(_ == success) match {
            case false => failureResponseO.fold(defaultFailureResponse)(response => Task(response))
            case true  => service(req.copy(method = success))
          }
        }
      }
      case None => service(req)
    }
  }
}