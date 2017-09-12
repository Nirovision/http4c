package com.imageintelligence.http4c.examples

import cats.effect.IO
import com.imageintelligence.http4c.ApiResponse
import com.imageintelligence.http4c.ApiResponseUtils
import org.http4s.dsl._
import org.http4s.HttpService

object ExampleApiResponse {

  val service = HttpService[IO] {
    case req @ POST -> Root => ApiResponseUtils.decodeAs[String, IO](req) { s =>
      Ok(ApiResponse.success(s))
    }

    case req @ GET -> Root => {
      Ok(ApiResponse.success("An Api Response"))
    }
  }

}
