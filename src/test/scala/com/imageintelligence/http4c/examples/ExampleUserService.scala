package com.imageintelligence.http4c.examples

import com.imageintelligence.http4c.DslHelpers
import com.imageintelligence.http4c.headers.`X-Proxy-Cache`
import org.http4s.ParseFailure
import org.http4s.HttpService
import org.http4s.dsl._
import cats._
import cats.effect.IO
import cats.implicits._

case class UserId(value: String)

sealed trait Gender
case object Male extends Gender
case object Female extends Gender

object Gender {
  def fromString(s: String): Either[String, Gender] = s.toLowerCase match {
    case "male" => Right(Male)
    case "female" => Right(Female)
    case other => Left(s"${other} is not a gender. Gender must be 'male' or 'female'")
  }
}

object ExampleUserService {

  implicit val GenderQueryParamDecoder =
    DslHelpers.queryParamDecoderFromEither(q => Gender.fromString(q).leftMap(x => ParseFailure.apply(x, x)))

  val GenderQueryParam = DslHelpers.optionalQueryParam[Gender]("gender")

  val UserIdMatcher = DslHelpers.validatingPathMatcher { x =>
    if (x.length > 6) Right(UserId(x))
    else Left("Bad user id")
  }

  val service = HttpService[IO] {
    case req @ GET -> Root / UserIdMatcher(userId) :? GenderQueryParam(gender)  => {
      userId match {
        case Right(id) => Ok(id.value).putHeaders(`X-Proxy-Cache`(false))
        case Left(error) => BadRequest(error)
      }
    }
  }

}
