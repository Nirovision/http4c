package com.imageintelligence.http4c.examples

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._
import org.http4s._
import org.http4s.dsl._
import com.imageintelligence.http4c.ArgonautInstances._

object ExampleArgonautService {
  case class Person(name: String, job: Option[String])

  implicit def CodecPerson: CodecJson[Person] =
    casecodec2(Person.apply, Person.unapply)("name", "job")

  val service = HttpService {
    case req @ POST -> Root => req.decode[Person] { person =>
      Ok(person.asJson)
    }
  }
}
