package com.imageintelligence.http4c.examples

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._
import org.http4s._
import org.http4s.dsl._
import com.imageintelligence.http4c.ArgonautInstances._

object ExampleArgonautService {
  case class Person(name: String)

  implicit def DecodePerson: DecodeJson[Person] = DecodeJson[Person](
    c => c.get[String]("name").map(Person.apply)
  )

  val service = HttpService {
    case req @ POST -> Root => req.decode[Person] { person =>
      Ok(person.toString)
    }
  }
}
