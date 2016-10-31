import Depend._

lazy val buildSettings = Seq(
  name := "http4c",
  organization := "com.imageintelligence",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  resolvers := Depend.depResolvers,
  libraryDependencies := Depend.dependencies,
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("imageintelligence")
)

lazy val root = (project in 
  file(".")).settings(buildSettings: _*)