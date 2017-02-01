package com.imageintelligence.http4c.examples

import com.imageintelligence.http4c.middleware._
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object ExampleService extends ServerApp {

  val compiledService = Router(
    "/health" -> ExampleHealthService.service,
    "/users"  -> ExampleUserService.service,
    "/bytes"  -> ExampleBytesService.service
  )

  val metricsMiddleware = MetricsMiddleware(x => println(x), (x, y, z) => println(x), "example")(_)
  val jsonLoggingMiddleware = LoggingMiddleware.jsonLoggingMiddleware(x => println(x))(_)
  val basicLoggingMiddleware = LoggingMiddleware.basicLoggingMiddleware(x => println(x))(_)
  val middlewareStack = metricsMiddleware andThen jsonLoggingMiddleware andThen basicLoggingMiddleware

  val compiledServiceWithMiddleware = middlewareStack(compiledService)

  def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      .mountService(compiledServiceWithMiddleware)
      .start
  }
}
