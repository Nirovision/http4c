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
    "org.http4s"  %% "http4s-dsl",
    "org.http4s"  %% "http4s-argonaut"
  ).map(_ % http4sVersion).map(_.withSources)

  lazy val http4sServer = Seq(
    "org.http4s"  %% "http4s-blaze-server"
  ).map(_ % http4sVersion).map(_ % "test")

  lazy val bucket4j = Seq(
    "com.github" % "bucket4j" % "1.3.0"
  )

  lazy val scalaTestCheck = Seq(
    "org.scalatest"   %% "scalatest"  % "2.2.4",
    "org.scalacheck"  %% "scalacheck" % "1.12.1"
  ).map(_.withSources).map(x => x.force()).map(_ % "test")

  lazy val depResolvers = Seq(
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    "JCenter Bintray Repo" at "http://jcenter.bintray.com",
    Resolver.sonatypeRepo("releases")
  )

  lazy val dependencies = 
    scalaz ++
    argonaut ++
    http4s ++
    http4sServer ++
    bucket4j ++
    scalaTestCheck
}
