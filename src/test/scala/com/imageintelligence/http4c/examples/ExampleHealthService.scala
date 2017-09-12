package com.imageintelligence.http4c.examples

import com.imageintelligence.http4c.Health._
import com.imageintelligence.http4c.HealthReportService
import cats.effect._

object ExampleHealthService {

  val pg = HealthCheck[IO]("postgres", IO(HealthCheckSuccess))
  val mo = HealthCheck[IO]("mongodb", IO(HealthCheckSuccess))
  val de = HealthCheck[IO]("detection-engine", IO(HealthCheckFailure(new Exception("Ava is broken"), true)))

  val report = HealthReport(List(pg, mo, de))

  val service = HealthReportService(report).service
}
