import Depend._

import scala.util.Try

lazy val buildSettings = Seq(
  name := "http4c",
  organization := "com.imageintelligence",
  version := Try(sys.env("LIB_VERSION")).getOrElse("1.0.0"),
  scalaVersion := "2.11.8",
  resolvers := Depend.depResolvers,
  libraryDependencies := Depend.dependencies,
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("imageintelligence")
)

lazy val root = (project in 
  file(".")).settings(buildSettings: _*)