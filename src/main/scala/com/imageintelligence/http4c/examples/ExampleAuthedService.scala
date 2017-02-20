package com.imageintelligence.http4c.examples

import com.auth0.jwt.interfaces.DecodedJWT

import scalaz._
import Scalaz._
import org.http4s.dsl._
import org.http4s.AuthedService

object ExampleAuthedService {

  val service= AuthedService[DecodedJWT] {
    case req @ _ -> Root as user => {
      Ok(s"Authed as user: ${user}")
    }
  }
}
