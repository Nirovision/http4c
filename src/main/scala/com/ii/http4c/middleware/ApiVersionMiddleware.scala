package com.ii.http4c.middleware

import com.ii.http4c.headers.`Accept-Version`
import org.http4s.HttpService
import org.http4s.Service

import scalaz._
import Scalaz._

object ApiVersionMiddleware {
  def apply(defaultService: HttpService, versions: Map[String, HttpService]): HttpService = Service.lift { req =>
    val serviceToServeWith = req.headers.get(`Accept-Version`) match {
      case Some(version) => versions.getOrElse(version.version, defaultService)
      case None => defaultService
    }
    serviceToServeWith(req)
  }
}
