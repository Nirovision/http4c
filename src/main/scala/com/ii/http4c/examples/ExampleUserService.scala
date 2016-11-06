package com.ii.http4c.examples

import com.ii.http4c.DslHelpers
import com.ii.http4c.DslHelpers.MatchPathVar
import org.http4s.ParseFailure
import org.http4s.HttpService
import org.http4s.dsl._

import scalaz._
import Scalaz._

case class UserId(value: String)

sealed trait Gender
case object Male extends Gender
case object Female extends Gender

object Gender {
  def fromString(s: String): String \/ Gender = s.toLowerCase match {
    case "male" => Male.right
    case "female" => Female.right
    case other => s"${other} is not a gender. Gender must be 'male' or 'female'".left
  }
}

object ExampleUserService {


  implicit val GenderQueryParamDecoder =
    DslHelpers.queryParamDecoderFromEither(q => Gender.fromString(q).leftMap(x => ParseFailure.apply(x, x)))

  val GenderQueryParam = DslHelpers.optionalQueryParam[Gender]("gender")

  val UserIdMatcher = DslHelpers.validatingPathMatcher { x =>
    if (x.length > 6) UserId(x).right
    else ParseFailure("Bad user id", "").left
  }

  val service = HttpService {
    case req @ GET -> Root / UserIdMatcher(userId) :? GenderQueryParam(gender)  => {
      userId match {
        case \/-(id) => Ok(id.value)
        case -\/(error) => BadRequest(error.sanitized)
      }
    }
  }

}
