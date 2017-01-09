package com.imageintelligence.http4c

import argonaut.{Argonaut, DecodeJson, EncodeJson, Json}
import org.http4s.Charset
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.MediaType
import org.http4s.argonaut.{ArgonautInstances => Ai}
import org.http4s.headers.`Content-Type`

object ArgonautInstances extends Ai {
  implicit def encodeFromEncodeJson[A](implicit codec: EncodeJson[A]): EntityEncoder[A] = {
    jsonEncoderOf(codec)
  }

  implicit def decoderFromDecodeJson[A](implicit codec: DecodeJson[A]): EntityDecoder[A] = {
    jsonOf(codec)
  }

  override implicit val jsonEncoder: EntityEncoder[Json] =
    EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[Json] { json =>
      // TODO naive implementation materializes to a String.
      // Look into replacing after https://github.com/non/jawn/issues/6#issuecomment-65018736
      Argonaut.nospace.copy(preserveOrder = true).pretty(json)


    }.withContentType(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
}
