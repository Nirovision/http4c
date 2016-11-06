package com.ii.http4c.examples

import org.http4s.HttpService
import org.http4s.dsl._

import scalaz.stream.Process
import scala.util.Random
import scalaz._, Scalaz._
import scalaz.concurrent.Task

object ExampleBytesService {

  val service: HttpService = HttpService {
    case req @ _ -> Root / IntVar(n) => {
      val randomBytes: Process[Task, Byte] = Process.repeatEval(
        Task { (Random.nextInt(256) - 128).toByte }
      )
      Ok(randomBytes.take(n))
    }
  }
}
