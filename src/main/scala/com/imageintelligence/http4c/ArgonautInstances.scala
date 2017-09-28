package com.imageintelligence.http4c

import argonaut.DecodeJson
import argonaut.EncodeJson
import argonaut.Json
import argonaut.PrettyParams
import cats.effect.Sync
import org.http4s._
import org.http4s.argonaut.{ArgonautInstances => Ai}
import org.http4s.headers.`Content-Type`
import cats.implicits._
import cats._

object Http4sAi extends Ai {
  def defaultPrettyParams: PrettyParams = PrettyParams.nospace.copy(
    preserveOrder = true,
    dropNullKeys = true
  )
}

object ArgonautInstances {

  def jsonEncoder[A, F[_]: Applicative](prettyParams: PrettyParams = Http4sAi.defaultPrettyParams): EntityEncoder[F, Json] = {
    val f = implicitly[Applicative[F]]
    EntityEncoder.stringEncoder[F](f, Charset.`UTF-8`).contramap[Json] { json =>
      prettyParams.pretty(json)
    }.withContentType(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
  }

  implicit def encoderFromEncodeJson[A, F[_]: Applicative](implicit encoder: EncodeJson[A]): EntityEncoder[F, A] = {
    val f = implicitly[Applicative[F]]
    Http4sAi.jsonEncoderOf[F, A](f, encoder)
  }

  implicit def decoderFromDecodeJson[A, F[_]: Sync](implicit decoder: DecodeJson[A]): EntityDecoder[F, A] = {
    Http4sAi.jsonDecoder[F].flatMapR { json =>
      decoder.decodeJson(json).fold(
        (message, history) => {
          val m = s"Could not decode JSON. Error: $message"
          DecodeResult.failure[F, A](MalformedMessageBodyFailure(m, Some(new Exception(message))))
        },
        DecodeResult.success(_)
      )
    }
  }
}
