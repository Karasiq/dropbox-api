lazy val commonSettings = Seq(
  organization := "com.github.karasiq",
  version := "1.0.1",
  isSnapshot := version.value.endsWith("SNAPSHOT"),
  // resolvers += Resolver.sonatypeRepo("snapshots"),
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3")
)

lazy val librarySettings = Seq(
  name := "dropbox-api",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-actor" % "2.5.2",
    "com.typesafe.akka" %% "akka-stream" % "2.5.2",
    "com.typesafe.akka" %% "akka-http" % "10.0.9",
    "com.dropbox.core" % "dropbox-core-sdk" % "3.0.5",
    "com.github.karasiq" %% "commons-configs" % "1.0.8"
  )
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ ⇒ false },
  licenses := Seq("Apache License, Version 2.0" → url("http://opensource.org/licenses/Apache-2.0")),
  homepage := Some(url("https://github.com/Karasiq/dropbox-api")),
  pomExtra := <scm>
    <url>git@github.com:Karasiq/dropbox-api.git</url>
    <connection>scm:git:git@github.com:Karasiq/dropbox-api.git</connection>
  </scm>
    <developers>
      <developer>
        <id>karasiq</id>
        <name>Piston Karasiq</name>
        <url>https://github.com/Karasiq</url>
      </developer>
    </developers>
)

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  publishArtifact in makePom := false,
  publishTo := Some(Resolver.file("Repo", file("target/repo")))
)

lazy val library = project
  .settings(commonSettings, librarySettings, publishSettings)

lazy val testApp = (project in file("test-app"))
  .settings(commonSettings, noPublishSettings)
  .dependsOn(library)

lazy val `dropbox-api` = (project in file("."))
  .settings(commonSettings, noPublishSettings)
  .aggregate(library)