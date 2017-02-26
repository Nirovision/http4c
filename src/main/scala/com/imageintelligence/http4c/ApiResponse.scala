package com.imageintelligence.http4c

import argonaut._
import Argonaut._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers._

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import com.imageintelligence.http4c.ArgonautInstances._

sealed trait ApiResponse[A] {
  def render: Json
}
case class SuccessResponse[A: EncodeJson](value: A) extends ApiResponse[A] {
  def render: Json = value.asJson
}
case class FailureResponse(message: String) extends ApiResponse[Nothing] {
  def render = Json("error" := message)
}

object ApiResponse {
  def success[A: EncodeJson](a: A): ApiResponse[A] =
    SuccessResponse(a)

  def failure(message: String): ApiResponse[Nothing] =
    FailureResponse(message)

  implicit def EncodeJsonApiResponse[A] = EncodeJson[ApiResponse[A]](c => c.render)

  implicit def ApiResponseEncoder[A]: EntityEncoder[ApiResponse[A]] = {
    EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[ApiResponse[A]] { r: ApiResponse[A] =>
      Argonaut.nospace.pretty(r.render)
    }.withContentType(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
  }
}

object ApiResponseUtils {

  def decodeAs[A: EntityDecoder](req: Request)(f: A => Task[Response]) = {
    req.attemptAs[A].run.flatMap {
      case \/-(a) => f(a)
      case -\/(e) => UnprocessableEntity(ApiResponse.failure(e.getMessage))
    }
  }
}
