package com.imageintelligence.http4c
import org.http4s._

package object middleware {

  type Middleware[F[_], A, B, C, D] = Service[F, A, B] => Service[F, C, D]

  type HttpMiddleware[F[_]] = Middleware[F, Request[F], MaybeResponse[F], Request[F], MaybeResponse[F]]

}
