package com.imageintelligence.http4c

import argonaut.DecodeJson
import org.http4s.EntityDecoder
import org.http4s.argonaut.{ArgonautInstances => Ai}

object ArgonautInstances extends Ai {
  implicit def decoderFromDecodeJson[A](implicit codec: DecodeJson[A]): EntityDecoder[A] = {
    jsonOf(codec)
  }
}
