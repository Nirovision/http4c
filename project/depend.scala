import sbt._

object Depend {
  lazy val scalazVersion = "7.1.7"

  lazy val scalaz = Seq(
    "org.scalaz" %% "scalaz-core"
  ).map(_ % scalazVersion)

  lazy val kindProjectors = Seq("org.spire-math" %% "kind-projector" % "0.8.1")

  lazy val argonaut = Seq("io.argonaut" %% "argonaut" % "6.1")

  lazy val http4s = Seq(
    "org.http4s"  %% "http4s-blaze-server",
    "org.http4s"  %% "http4s-dsl",
    "org.http4s"  %% "http4s-argonaut"
  ).map (x => (x % "0.12.3").withSources)

  lazy val scalaTestCheck = Seq(
    "org.scalatest"   %% "scalatest"                 % "2.2.4",
    "org.scalacheck"  %% "scalacheck"                % "1.12.2",
    "org.scalaz"      %% "scalaz-scalacheck-binding" % scalazVersion
  ).map(_.withSources).map(_ % "test")

  lazy val depResolvers = Seq(
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("releases")
  )

  lazy val dependencies = 
    scalaz ++
    kindProjectors ++
    argonaut ++
    http4s ++
    scalaTestCheck 

}
