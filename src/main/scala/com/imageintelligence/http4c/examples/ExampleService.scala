package com.imageintelligence.http4c.examples

import com.ii.http4c.middleware.LoggingMiddleware
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

  val middlewareStack = LoggingMiddleware.basicLoggingMiddleware(x => println(x))

  val compiledServiceWithMiddleware = middlewareStack(compiledService)

  def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      .mountService(compiledServiceWithMiddleware)
      .start
  }
}
