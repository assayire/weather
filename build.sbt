import Modules.*

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "jh"
ThisBuild / scalaVersion := "3.5.0"

lazy val app =
  project
    .in(file("app"))
    .settings(
      libraryDependencies ++=
        Cats.Effect ::
          Cats.Logging ::
          Circe :::
          Http4s.All :::
          Logback.All :::
          OtherDevStuff :::
          Sttp.All :::
          UnitTesting.All
    )

lazy val weather =
  project
    .in(file("."))
    .aggregate(app)
