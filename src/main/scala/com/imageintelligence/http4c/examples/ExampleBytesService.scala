package com.imageintelligence.http4c.examples

import org.http4s.HttpService
import org.http4s.dsl._
import scodec.bits.ByteVector

import scalaz.stream.Process
import scala.util.Random
import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object ExampleBytesService {

  val service: HttpService = HttpService {
    case req @ _ -> Root / IntVar(n) => {
      val randomBytes: Process[Task, Byte] = Process.repeatEval(
        Task { (Random.nextInt(256) - 128).toByte }
      )
      Ok(ByteVector.apply(randomBytes.take(n).runLog.run))
    }
  }
}
