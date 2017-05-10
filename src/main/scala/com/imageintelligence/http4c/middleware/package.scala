package com.imageintelligence.http4c
import org.http4s._

package object middleware {

  type Middleware[A, B, C, D] = Service[A, B] => Service[C, D]

  type HttpMiddleware = Middleware[Request, Response, Request, Response]

}
