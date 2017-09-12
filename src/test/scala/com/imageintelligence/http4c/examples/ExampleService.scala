package com.imageintelligence.http4c.examples

import fs2.Stream
import cats.effect.IO
import com.imageintelligence.http4c.middleware._
import org.http4s._
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object ExampleService extends StreamApp[IO] {


  val compiledService = Router(
    "/health"       -> ExampleHealthService.service,
    "/users"        -> ExampleUserService.service,
    "/bytes"        -> ExampleBytesService.service,
    "/argonaut"     -> ExampleArgonautService.service,
    "/api-response" -> ExampleApiResponse.service
  )

  val metricsMiddleware = MetricsMiddleware[IO](x => println(x), (x, y, z) => println(s"${x}, ${y}, ${z}"), "example")
  val jsonLoggingMiddleware = LoggingMiddleware.jsonLoggingMiddleware[IO](x => println(x), LoggingMiddleware.toLog)
  val basicLoggingMiddleware = LoggingMiddleware.basicLoggingMiddleware[IO](x => println(x), LoggingMiddleware.toLog)
  val middlewareStack = metricsMiddleware andThen jsonLoggingMiddleware andThen basicLoggingMiddleware

  val compiledServiceWithMiddleware = middlewareStack(compiledService)

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, Nothing] = {
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(compiledServiceWithMiddleware)
      .serve
  }
}
