package com.imageintelligence.http4c

import argonaut._
import Argonaut._
import cats._
import cats.implicits._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers._
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

  implicit def ApiResponseEncoder[F[_]: Applicative, A]: EntityEncoder[F, ApiResponse[A]] = {
    val f = implicitly[Applicative[F]]
    EntityEncoder.stringEncoder[F](f, Charset.`UTF-8`).contramap[ApiResponse[A]] { r: ApiResponse[A] =>
      Http4sAi.defaultPrettyParams.pretty(r.render)
    }.withContentType(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
  }
}

object ApiResponseUtils {

  def decodeAs[A, F[_]: Monad](req: Request[F])(f: A => F[Response[F]])(implicit decoder: EntityDecoder[F, A]) = {
    decoder.decode(req, strict = false).fold(
      failure => Response[F](Status.UnprocessableEntity).withBody[ApiResponse[Nothing]](ApiResponse.failure(failure.getMessage)),
      success => f(success)
    ).flatten
  }
}
