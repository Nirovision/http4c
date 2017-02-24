package com.imageintelligence.http4c

//import argonaut.PrettyParams
//import argonaut._
import argonaut.DecodeJson
import argonaut.EncodeJson
import argonaut.Json
import argonaut.PrettyParams
import argonaut.Argonaut
import org.http4s._
import jawn.jawnDecoder
import org.http4s.argonaut.{ArgonautInstances => Ai}
import org.http4s.headers.`Content-Type`
import scalaz._, Scalaz._

object Http4sAi extends Ai {
  protected def defaultPrettyParams: PrettyParams = PrettyParams.spaces2
}

object ArgonautInstances {

  protected def defaultPrettyParams: PrettyParams = PrettyParams.nospace

  def jsonEncoder[A](prettyParams: PrettyParams = defaultPrettyParams): EntityEncoder[Json] = {
    EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[Json] { json =>
      prettyParams.pretty(json)
    }.withContentType(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
  }

  implicit def encoderFromEncodeJson[A](implicit encoder: EncodeJson[A]): EntityEncoder[A] = {
    Http4sAi.jsonEncoderOf(encoder)
  }

  implicit def decoderFromDecodeJson[A](implicit decoder: DecodeJson[A]): EntityDecoder[A] = {
    Http4sAi.jsonDecoder.flatMapR { json =>
      decoder.decodeJson(json).fold(
        (message, history) => {
          val m = s"Could not decode JSON. Error: $message"
          DecodeResult.failure(GenericDecodeFailure(m, v => Response(Status.UnprocessableEntity, v).withBody(m)))
        },
        DecodeResult.success(_)
      )
    }
  }
}
