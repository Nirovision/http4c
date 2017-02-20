import sbt._

object Depend {
  lazy val scalazVersion = "7.2.6"
  lazy val http4sVersion = "0.15.2a"
  lazy val argonautVersion = "6.2-RC2"

  lazy val scalaz = Seq(
    "org.scalaz" %% "scalaz-core"
  ).map(_ % scalazVersion)

  lazy val argonaut = Seq("io.argonaut" %% "argonaut" % argonautVersion)

  lazy val http4s = Seq(
    "org.http4s"  %% "http4s-blaze-server",
    "org.http4s"  %% "http4s-dsl",
    "org.http4s"  %% "http4s-argonaut"
  ).map(_ % http4sVersion).map(_.withSources)

  lazy val jwt = Seq(
    "com.auth0" % "java-jwt" % "3.0.1"
  )

  lazy val scalaTestCheck = Seq(
    "org.scalatest"   %% "scalatest"  % "2.2.4",
    "org.scalacheck"  %% "scalacheck" % "1.12.1"
  ).map(_.withSources).map(x => x.force()).map(_ % "test")

  lazy val depResolvers = Seq(
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("releases")
  )

  lazy val dependencies = 
    scalaz ++
    argonaut ++
    http4s ++
    jwt ++
    scalaTestCheck
}
