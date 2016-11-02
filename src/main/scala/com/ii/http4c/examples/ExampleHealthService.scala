package com.ii.http4c.examples

import com.ii.http4c.Health._
import com.ii.http4c.HealthReportService

import scalaz.concurrent.Task

object ExampleHealthService {

  val pg = HealthCheck("postgres", Task.now(HealthCheckSuccess))
  val mo = HealthCheck("mongodb", Task.now(HealthCheckSuccess))
  val de = HealthCheck("detection-engine", Task.now(HealthCheckFailure(new Exception("Skynet is broken"), true)))

  val report = HealthReport(List(pg, mo, de))

  val service = HealthReportService(report)
}
