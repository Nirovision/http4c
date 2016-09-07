import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

import com.ambiata.promulgate.project.ProjectPlugin._

object build extends Build {
  type Settings = Def.Setting[_]

  lazy val http4c = project(
      "http4c"
    , "."
    , packageSettings(None) ++ Seq[Settings](
      libraryDependencies ++=
            depend.argonaut
        ++  depend.scalaz
      ) ++ lib("http4c")
  ).dependsOn()

  lazy val standardSettings = Defaults.coreDefaultSettings ++
                              projectSettings              ++
                              compilationSettings          ++
                              testingSettings              ++
                              Seq(resolvers ++= depend.resolvers)

  lazy val projectSettings: Seq[Settings] = Seq(
      name := "http4c"
    , version in ThisBuild := "0.0.0"
    , organization := "com.cammy"
    , scalaVersion := "2.11.4"
    , crossScalaVersions := Seq(scalaVersion.value)
    // https://gist.github.com/djspiewak/976cd8ac65e20e136f05
    , unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / s"scala-${scalaBinaryVersion.value}"
    , updateOptions := updateOptions.value.withCachedResolution(true)
    , publishArtifact in (Test, packageBin) := true
  ) ++ Seq(prompt)


  lazy val compilationSettings: Seq[Settings] = Seq(
    javacOptions ++= Seq("-Xmx3G", "-Xms512m", "-Xss4m"),
    maxErrors := 10,
    scalacOptions <++= scalaVersion.map({
      case x if x.contains("2.11") => Seq("-deprecation", "-unchecked", "-feature", "-language:_", "-Xlint")
      case x if x.contains("2.10") => Seq("-deprecation", "-unchecked", "-feature", "-language:_", "-Ywarn-all", "-Xlint")
      case x => sys.error("Unsupported scala version: " + x)
    }),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )

  def lib(name: String): Seq[Settings] =
    promulgate.library(s"com.cammy.$name", "cammy-ivy-internal")

  lazy val testingSettings: Seq[Settings] = Seq(
    logBuffered := false,
    cancelable := true,
    javaOptions += "-Xmx3G"
  )

  def packageSettings(main: Option[String]) =
    assemblySettings ++
    Seq(
      mainClass := main,
      test in assembly := { },
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { mergeStrategy => {
        case entry =>
          val strategy = mergeStrategy(entry)
          if (strategy == MergeStrategy.deduplicate) MergeStrategy.first
          else strategy
        }
      }
    )

  lazy val prompt = shellPrompt in ThisBuild := { state =>
    val name = Project.extract(state).currentRef.project
    (if (name == "http4c") "" else name) + "> "
  }

  def aggregateProject(pname: String, dir: String, extra: Seq[Settings], subprojects: Seq[ProjectReference]): Project = Project(
      id = pname
    , base = file(dir)
    , settings = standardSettings ++ extra
    , aggregate = subprojects
    )

  def project(pname: String, dir: String, extra: Seq[Settings]): Project = Project(
      id = pname
    , base = file(dir)
    , settings = standardSettings ++ Seq[Settings](
          name := pname
        ) ++ extra
    )

}
