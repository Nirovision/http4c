package com.imageintelligence.http4c.examples

import cats.effect.IO
import org.http4s.HttpService
import org.http4s.dsl._
import org.http4s.headers.Authorization
import scala.util.Random
import fs2._

object ExampleBytesService {

  val service: HttpService[IO] = HttpService {
    case req @ _ -> Root / IntVar(n) => {
      println(req.headers.get(Authorization))
      val randomBytes: Stream[IO, Byte] = Stream.repeatEval(
        IO {
          (Random.nextInt(256) - 128).toByte
        }
      )
      Ok(randomBytes)
    }
  }
}
