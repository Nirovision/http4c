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

sealed trait JWTAuthError {
  def renderSanitized: String = this match {
    case InvalidHeaderType(header) => s"Invalid Authorization value. Must be of type Bearer, not ${header}"
    case NoAuthorizationHeader => s"Could not find an Authorization header"
    case UnknownErrorVerifyingJWT(_) => s"Error verifying token"
  }

  def render: String = this match {
    case InvalidHeaderType(_) => this.renderSanitized
    case NoAuthorizationHeader => this.renderSanitized
    case UnknownErrorVerifyingJWT(e) => this.renderSanitized + s": ${e.getMessage}"
  }
}
case class InvalidHeaderType(header: String) extends JWTAuthError
case object NoAuthorizationHeader extends JWTAuthError
case class UnknownErrorVerifyingJWT(e: Throwable) extends JWTAuthError

object JWTAuthMiddleware {

  def getBearerToken(req: Request): JWTAuthError \/ String = {
    req.headers.get(Authorization) match {
      case Some(Authorization(OAuth2BearerToken(token))) => token.right
      case Some(header) => InvalidHeaderType(header.toString).left
      case None => NoAuthorizationHeader.left
    }
  }

  def decodeJWT(token: String, secret: String): JWTAuthError \/ DecodedJWT = {
    \/.fromTryCatchNonFatal {
      JWT.require(Algorithm.HMAC256(secret)).build().verify(token)
    }.leftMap {
      case e => UnknownErrorVerifyingJWT(e)
    }
  }

  private def defaultError(error: JWTAuthError) = {
    Forbidden(error.render)
  }

  def apply[A](secret: String, parse: DecodedJWT => JWTAuthError \/ A, onError: JWTAuthError => Task[Response] = defaultError): AuthMiddleware[A] = {

    val authUser: Kleisli[Task, Request, JWTAuthError \/ A] = Kleisli { req: Request =>
      Task {
        for {
          bearerToken <- getBearerToken(req)
          decodedJWT  <- decodeJWT(bearerToken, secret)
          parsedJWT   <- parse(decodedJWT)
        } yield parsedJWT
      }
    }

    val onFailure: AuthedService[JWTAuthError] = Kleisli(error => onError(error.authInfo))

    AuthMiddleware(authUser, onFailure)
  }

}
