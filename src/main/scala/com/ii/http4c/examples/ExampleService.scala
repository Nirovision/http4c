package com.ii.http4c.examples

import com.ii.http4c.Health.HealthCheck
import com.ii.http4c.Health.HealthCheckFailure
import com.ii.http4c.Health.HealthReport
import com.ii.http4c._
import org.http4s.dsl._
import org.http4s.ParseFailure
import org.http4s.HttpService
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

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


object ExampleService extends ServerApp {

  val UserIdMatcher = Helpers.pathMatcher(x => Some(UserId(x)))

  implicit val GenderQueryParamDecoder =
    Helpers.queryParamDecoderFromEither(q => Gender.fromString(q).leftMap(x => ParseFailure.apply(x, x)))

  val GenderQueryParam = Helpers.optionalQueryParam[Gender]("gender")

  val userService = HttpService {
    case req @ GET -> Root / UserIdMatcher(userId) :? GenderQueryParam(gender)  => {
      Ok(userId.value)
    }
  }

  val healthService = ExampleHealthService.service

  val compiledService = Router(
    "/health" -> healthService.service,
    "/userService" -> userService
  )

  def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      .mountService(compiledService)
      .start
  }
}
