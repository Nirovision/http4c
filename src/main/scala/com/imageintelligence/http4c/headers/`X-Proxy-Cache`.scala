package com.imageintelligence.http4c.headers

import scalaz._, Scalaz._
import org.http4s.util.Writer
import org.http4s.dsl._
import org.http4s._

final case class `X-Proxy-Cache`(cacheHit: Boolean) extends Header.Parsed {
  override def key = `X-Proxy-Cache`
  override def renderValue(writer: Writer): writer.type = {
    if (cacheHit) {
      writer.append("HIT")
    } else {
      writer.append("MISS")
    }
  }
}

object `X-Proxy-Cache` extends HeaderKey.Singleton {
  type HeaderT = `X-Proxy-Cache`
  def name = "X-Proxy-Cache".ci

  def parse(s: String): ParseResult[`X-Proxy-Cache`] = {
    s match {
      case "HIT" => `X-Proxy-Cache`(true).right
      case "MISS" => `X-Proxy-Cache`(false).right
      case other => {
        val e = s"$other is not a valid X-Proxy-Cache header"
        ParseFailure.apply(e, e).left
      }
    }
  }

  def matchHeader(header: Header): Option[`X-Proxy-Cache`] = {
    if (header.name == name)
      parse(header.value).toOption
    else None
  }
}
