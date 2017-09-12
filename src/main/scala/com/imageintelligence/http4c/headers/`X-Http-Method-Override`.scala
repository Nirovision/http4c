package com.imageintelligence.http4c.headers

import org.http4s.HeaderKey
import org.http4s.dsl._
import org.http4s._
import org.http4s.util.Writer
import scala.util.Try

final case class `X-Http-Method-Override`(method: String) extends Header.Parsed {
  override def key = `X-Http-Method-Override`
  override def renderValue(writer: Writer): writer.type =
    writer.append(method)
}

object `X-Http-Method-Override` extends HeaderKey.Singleton {
  type HeaderT = `X-Http-Method-Override`
  def name = "X-HTTP-Method-Override".ci
  def matchHeader(header: Header): Option[`X-Http-Method-Override`] = {
    if (header.name == name)
      Try(`X-Http-Method-Override`(header.value)).toOption
    else None
  }

  def parse(s: String): ParseResult[`X-Http-Method-Override`] = {
    Right(`X-Http-Method-Override`(s))
  }
}
