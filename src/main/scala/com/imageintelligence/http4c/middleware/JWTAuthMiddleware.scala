package com.imageintelligence.http4c.middleware

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object JWTAuthMiddleware {

  def getBearerToken(req: Request): Throwable \/ String = {
    req.headers.get(Authorization) match {
      case Some(Authorization(OAuth2BearerToken(token))) => token.right
      case Some(_) => new Exception("Authorization header was not of type Bearer").left
      case None => new Exception("Couldn't find an Authorization header").left
    }
  }

  def decodeJWT(token: String, secret: String): Throwable \/ DecodedJWT = {
    \/.fromTryCatchNonFatal {
      JWT.require(Algorithm.HMAC256(secret)).build().verify(token)
    }
  }

  def apply[A](secret: String, parse: DecodedJWT => Throwable \/ A): AuthMiddleware[A] = {

    val authUser: Kleisli[Task, Request, Throwable \/ A] = Kleisli { req: Request =>
      Task {
        for {
          bearerToken <- getBearerToken(req)
          decodedJWT  <- decodeJWT(bearerToken, secret)
          parsedJWT   <- parse(decodedJWT)
        } yield parsedJWT
      }
    }

    val onFailure: AuthedService[Throwable] = Kleisli(req => Forbidden(req.authInfo.getMessage))

    AuthMiddleware(authUser, onFailure)
  }

}
