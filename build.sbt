import sbt.Keys.libraryDependencies

lazy val Versions = new {
  val netty = "4.1.43.Final"
  val slf4j = "1.7.28"
}

lazy val scalaSettings = Seq(
  scalaVersion := "2.12.10",
  crossScalaVersions := List(scalaVersion.value, "2.13.1"),
  scalacOptions += "-deprecation",
  scalacOptions += "-unchecked",
  scalacOptions += "-feature"
)

lazy val javaSettings = Seq(
  crossPaths := false,
  autoScalaLibrary := false
)

lazy val commonSettings = Seq(
  organization := "com.avast.continuity",
  version := sys.env.getOrElse("TRAVIS_TAG", "0.1-SNAPSHOT"),
  description := "Library for passing context between threads in multi-threaded applications",
  licenses ++= Seq("MIT" -> url(s"https://github.com/avast/continuity/blob/${version.value}/LICENSE")),
  publishArtifact in Test := false,
  publishArtifact in (Compile, packageDoc) := false,
  sources in (Compile, doc) := Seq.empty,
  bintrayOrganization := Some("avast"),
  bintrayPackage := "continuity",
  pomExtra := (
    <scm>
      <url>git@github.com:avast/continuity.git</url>
      <connection>scm:git:git@github.com:avast/continuity.git</connection>
    </scm>
      <developers>
        <developer>
          <id>avast</id>
          <name>Jakub Janecek, Avast Software s.r.o.</name>
          <url>https://www.avast.com</url>
        </developer>
      </developers>
  ),
  libraryDependencies ++= Seq(
    "junit" % "junit" % "4.12" % "test",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test", // Required by sbt to execute JUnit tests
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
  ),
  testOptions += Tests.Argument(TestFrameworks.JUnit)
)

lazy val root = (project in file("."))
  .settings(
    name := "continuity",
    publish := {},
    publishLocal := {}
  )
  .aggregate(coreScala, core, nettyScala, netty, monix)

lazy val coreScala = (project in file("core-scala")).settings(
  commonSettings,
  scalaSettings,
  name := "continuity-core-scala",
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % Versions.slf4j
  )
)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    scalaSettings,
    name := "continuity-core"
  )
  .dependsOn(coreScala)

lazy val nettyScala = (project in file("netty-scala"))
  .settings(
    commonSettings,
    scalaSettings,
    name := "continuity-netty-scala",
    libraryDependencies ++= Seq(
      "io.netty" % "netty-transport" % Versions.netty
    )
  )
  .dependsOn(coreScala)

lazy val netty = (project in file("netty"))
  .settings(
    commonSettings,
    scalaSettings,
    name := "continuity-netty"
  )
  .dependsOn(nettyScala)

lazy val monix = (project in file("monix"))
  .settings(
    commonSettings,
    scalaSettings,
    name := "continuity-monix",
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.1.0"
    )
  )
  .dependsOn(coreScala)
