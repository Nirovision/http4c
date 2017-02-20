package com.imageintelligence.http4c.middleware

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object JWTAuthMiddleware {

  def getBearerToken(req: Request): String \/ String = {
    req.headers.get(Authorization) match {
      case Some(Authorization(OAuth2BearerToken(token))) => token.right
      case None => "Couldn't find an Authorization header".left
    }
  }

  def decodeJWT(token: String, secret: String): String \/ DecodedJWT = {
    \/.fromTryCatchNonFatal {
      JWT.require(Algorithm.HMAC256(secret)).build().verify(token)
    }.leftMap {
      case e: JWTVerificationException => e.getMessage
    }
  }

  def apply[A](secret: String, parse: DecodedJWT => String \/ A): AuthMiddleware[A] = {

    val authUser: Kleisli[Task, Request, String \/ A] = Kleisli { req: Request =>
      Task {
        for {
          bearerToken <- getBearerToken(req)
          decodedJWT  <- decodeJWT(bearerToken, secret)
          parsedJWT   <- parse(decodedJWT)
        } yield parsedJWT
      }
    }

    val onFailure: AuthedService[String] = Kleisli(req => Forbidden(req.authInfo))

    AuthMiddleware(authUser, onFailure)
  }

}
