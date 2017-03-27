package com.imageintelligence.http4c.middleware

import java.time.Duration
import com.github.bucket4j._
import org.http4s.HttpService
import org.http4s.Request
import org.http4s.dsl._
import org.http4s.server.HttpMiddleware

object RateLimitingMiddleware {

  private val buckets = new java.util.concurrent.ConcurrentHashMap[String, Bucket]()

  private def acquire(token: String, bucketingFn: => Bucket): Boolean = {
    val bucket = Option(buckets.get(token)) match {
      case None    => {
        val newBucket = bucketingFn
        Option(buckets.putIfAbsent(token, newBucket)).getOrElse(newBucket)
      }
      case Some(b) => b
    }

    bucket.tryConsumeSingleToken()
  }

  /**
    *
    * @param tokenFn A function from a Request => String which we will use to identify similar requests.
    * @param bucketingFn A function yielding a bucket4j Bucket.
    */
  def apply[A](tokenFn: Request => String, bucketingFn: => Bucket): HttpMiddleware = { service =>
    HttpService.lift { req =>
      if (acquire(tokenFn(req), bucketingFn))
        service.run(req)
      else
        TooManyRequests()
    }
  }

  def simpleThrottling(tokenFn: Request => String, capacity: Long, period: Duration): HttpMiddleware = {
    val limit = Bandwidth.simple(capacity, period)
    val bucket = Bucket4j.builder().addLimit(limit).build()
    apply(tokenFn, bucket)
  }
}
