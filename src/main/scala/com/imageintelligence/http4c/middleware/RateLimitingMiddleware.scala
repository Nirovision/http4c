package com.imageintelligence.http4c.middleware

import java.time.Duration

import com.github.bucket4j._
import org.http4s.Response
import org.http4s.Service
import org.http4s.dsl._
import org.http4s.server.Middleware

object RateLimitingMiddleware {

  /**
    *
    * @param tokenFn A function from a Request => String which we will use to identify similar requests.
    * @param bucketingFn A function yielding a bucket4j Bucket.
    */
  def apply[A, B](tokenFn: A => B, bucketingFn: => Bucket): Middleware[A, Response, A, Response] = { service =>

    val buckets = new java.util.concurrent.ConcurrentHashMap[B, Bucket]()

    Service.lift[A, Response] { req =>
      val token = tokenFn(req)

      val bucket = Option(buckets.get(token)) match {
        case None    => {
          val newBucket = bucketingFn
          Option(buckets.putIfAbsent(token, newBucket)).getOrElse(newBucket)
        }
        case Some(b) => b
      }
      
      if (bucket.tryConsumeSingleToken())
        service.run(req)
      else
        TooManyRequests()
    }
  }

  def simpleThrottling[A, B](tokenFn: A => B, capacity: Long, period: Duration): Middleware[A, Response, A, Response] = {
    val limit = Bandwidth.simple(capacity, period)
    val bucket = Bucket4j.builder().addLimit(limit).build()
    apply(tokenFn, bucket)
  }
}
