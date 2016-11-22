package com.imageintelligence.http4c.headers

import scalaz._, Scalaz._
import org.http4s.HeaderKey
import org.http4s.dsl._
import org.http4s._
import org.http4s.util.CaseInsensitiveString
import org.http4s.util.Writer
import scala.util.Try


final case class `Accept-Version`(version: String) extends Header.Parsed {
  override def key = `Accept-Version`
  override def renderValue(writer: Writer): writer.type =
    writer.append(version)
}

object `Accept-Version` extends HeaderKey.Singleton {
  type HeaderT = `Accept-Version`
  def name = "Accept-Version".ci
  def matchHeader(header: Header): Option[`Accept-Version`] = {
    if (header.name == name)
      Try(`Accept-Version`(header.value)).toOption
    else None
  }

  def parse(s: String): ParseResult[`Accept-Version`] = {
    `Accept-Version`(s).right
  }
}