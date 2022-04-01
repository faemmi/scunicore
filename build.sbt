import scala.sys.process._
val gitVersion = "git describe --always".!!.trim
val buildNum = Option(System.getProperty("build.number")).getOrElse("local")

// If there is a Tag starting with v, e.g. v0.3.0 use it as the build artefact version (e.g. 0.3.0)
val versionTag = sys.env
  .get("CI_COMMIT_TAG")
  .filter(_.startsWith("v"))
  .map(_.stripPrefix("v"))

val snapshotVersion = "0.1.0-SNAPSHOT"
val artefactVersion = versionTag.getOrElse(snapshotVersion)

ThisBuild / organization := "hpc.unicore"
ThisBuild / version := artefactVersion
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / scalacOptions += "-feature"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")
ThisBuild / scalacOptions += "-Ymacro-annotations"
ThisBuild / autoAPIMappings := true
ThisBuild / developers := List(
  Developer(
    id = "faemmi",
    name = "Fabian Emmerich",
    email = "mail@emmerichs.eu",
    url = url("https://github.com/faemmi")
  )
)
ThisBuild / test in publish := {}
ThisBuild / test in publishLocal := {}

Test / parallelExecution := true
Test / fork := false
IntegrationTest / parallelExecution := false
IntegrationTest / fork := true

concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)

val akkaVersion = "2.6.14"
val akkaHttpVersion = "10.2.4"
val scalaTestVersion = "3.0.9"
val circeVersion = "0.13.0"
val slf4jVersion = "1.7.30"
val scalaLoggingVersion = "3.9.3"

val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
val circeParser = "io.circe" %% "circe-parser" % circeVersion
val circeGenericExtras = "io.circe" %% "circe-generic-extras" % circeVersion
// logging
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
// testing framework
val scalatest = "org.scalatest" %% "scalatest" % scalaTestVersion

def configureBuildInfo(packageName: String): Seq[Def.Setting[_]] =
  Seq(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      BuildInfoKey("gitVersion", gitVersion),
      BuildInfoKey("buildNum", buildNum)
    ),
    buildInfoPackage := packageName
  )

lazy val root = (project in file("."))
  .settings(
    name := "scunicore",
    libraryDependencies ++= Seq(
      akkaActor,
      akkaHttp,
      akkaStream,
      circeGeneric,
      circeParser,
      circeGenericExtras,
      scalaLogging,
      scalatest
    ),
    publish := {},
    publishLocal := {},
    test := {},
    publishArtifact := false
  )
  .enablePlugins(ScalaUnidocPlugin)
  .settings(configureBuildInfo("hpc.unicore.buildinfo"))
