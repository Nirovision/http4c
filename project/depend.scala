import sbt._
import Keys._

object depend {
  val scalazVersion = "7.1.7"

  val scalaz = Seq(
    "org.scalaz" %% "scalaz-core"
  ).map(_ % scalazVersion)

  val kindProjectors = "org.spire-math" %% "kind-projector" % "0.8.1"

  val scalazTest = Seq(
    "org.scalaz" %% "scalaz-scalacheck-binding"
  ).map(_ % scalazVersion)

  val argonaut = Seq("io.argonaut" %% "argonaut" % "6.1")

  val http4s = Seq(
    "org.http4s"  %% "http4s-blaze-server",
    "org.http4s"  %% "http4s-dsl",
    "org.http4s"  %% "http4s-argonaut"
  ).map (x => (x % "0.12.3").withSources)

  val scalaTest = Seq(
    "org.scalatest"   %% "scalatest"  % "2.2.4",
    "org.scalacheck"  %% "scalacheck" % "1.12.2"
  ).map(_.withSources)

  val test = (scalazTest ++ scalaTest).map(_ % "test")

  val resolvers = Seq(
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("releases")
  )

}
