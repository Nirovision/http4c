package com.imageintelligence.http4c

import org.http4s._
import org.http4s.dsl._
import cats._
import cats.data.Validated
import cats.implicits._

object DslHelpers {

  case class MatchPathVar[A](f: String => Option[A]) {
    def unapply(str: String): Option[A] = f(str)
  }

  def pathMatcher[A](f: String => Option[A]): MatchPathVar[A] = {
    MatchPathVar(f)
  }

  def validatingPathMatcher[E, A](f: String => Either[E, A]): MatchPathVar[Either[E, A]] = {
    val v: String => Option[Either[E, A]] = f(_) match {
      case Right(a)   => Some(Right(a))
      case Left(e) => Some(Left(e))
    }
    MatchPathVar(v)
  }

  def queryParamDecoderFromEither[A](f: String => Either[ParseFailure,A]): QueryParamDecoder[A] = {
    new QueryParamDecoder[A] {
      def decode(value: QueryParameterValue) = {
        Validated.fromEither(f(value.value)).toValidatedNel
      }
    }
  }

  def optionalQueryParam[A: QueryParamDecoder](name: String): OptionalQueryParamDecoderMatcher[A] = {
    object QueryParam extends OptionalQueryParamDecoderMatcher[A](name)
    QueryParam
  }

  def requiredQueryParam[A: QueryParamDecoder](name: String): QueryParamDecoderMatcher[A] = {
    object QueryParam extends QueryParamDecoderMatcher[A](name)
    QueryParam
  }

}
