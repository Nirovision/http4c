package com.imageintelligence.http4c.middleware

import com.imageintelligence.http4c.headers.`Accept-Version`
import org.http4s.HttpService
import org.http4s.Service
import cats._

object ApiVersionMiddleware {
  def apply[F[_]: Applicative](defaultService: HttpService[F], versions: Map[String, HttpService[F]]): HttpService[F] = Service.lift { req =>
    val serviceToServeWith = req.headers.get(`Accept-Version`) match {
      case Some(version) => versions.getOrElse(version.version, defaultService)
      case None => defaultService
    }
    serviceToServeWith(req)
  }
}
