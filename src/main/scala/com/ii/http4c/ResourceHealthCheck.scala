//package com.ii.http4c
//
//import java.io.File
//
//import org.http4s.HttpService
//import org.http4s.dsl._
//import argonaut._
//import Argonaut._
//import scalaz._, Scalaz._
//import scala.io.Codec
//import scalaz.concurrent.Task
//
//sealed trait HealthCheckResult
//final case object HealthCheckSuccess extends HealthCheckResult
//final case class HealthCheckFailure(reason: Throwable) extends HealthCheckResult
//
//case class HealthCheck(results: List[(String, HealthCheckResult)])
//case class HealthReport(check: Task[HealthCheck])
//
//object HealthReport {
//  implicit object HealthCheckMonoid extends Monoid[HealthReport] {
//    def zero: HealthReport = {
//      HealthReport(Task.now(HealthCheck(List.empty[(String, HealthCheckResult)])))
//    }
//    def append(h1: HealthReport, h2: => HealthReport): HealthReport = {
//      val report = for {
//        f <- h1.check
//        g <- h2.check
//      } yield HealthCheck(f.results ++ g.results)
//      HealthReport(report)
//    }
//  }
//}
//
//object Example2 {
//  def main(args: Array[String]) {
//
//
//    val pg = HealthReport(Task {
//      HealthCheck(List(("postgres", HealthCheckSuccess)))
//    })
//
//    val mongo = HealthReport(Task {
//      HealthCheck(List(("mongo", HealthCheckFailure(new Exception("")))))
//    })
//
//    val combined = pg |+| mongo
//
//    val r = combined.check.run
//    println(r)
//
//  }
//}
