import sbt.*

object Modules {
  final val Laika    = "org.planet42"        %% "laika-core"   % "0.19.5"
  final val FlexMark = "com.vladsch.flexmark" % "flexmark-all" % "0.64.8"

  val Circe: List[ModuleID] = (
    "io.circe"   %% "circe-core" ::
      "io.circe" %% "circe-generic" ::
      "io.circe" %% "circe-parser" ::
      Nil
  ).map(_ % "0.14.9")

  val OtherDevStuff: List[ModuleID] =
    "org.planet42"          %% "laika-core"               % "0.19.5" ::
      "com.vladsch.flexmark" % "flexmark-all"             % "0.64.8" ::
      "org.commonmark"       % "commonmark"               % "0.23.0" ::
      "org.commonmark"       % "commonmark-ext-footnotes" % "0.23.0" ::
      Nil

  object Cats {
    val Effect  = "org.typelevel" %% "cats-effect"    % "3.5.4"
    val Logging = "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
  }

  object Logback {
    val Classic = "ch.qos.logback" % "logback-classic" % "1.5.8"
    val Core    = "ch.qos.logback" % "logback-core"    % "1.5.8"

    val All: List[ModuleID] = Classic :: Core :: Nil
  }

  object Http4s {
    val Version = "0.23.28"

    val Circe       = "org.http4s" %% "http4s-circe"        % Version
    val Dsl         = "org.http4s" %% "http4s-dsl"          % Version
    val EmberServer = "org.http4s" %% "http4s-ember-server" % Version

    val All: List[ModuleID] = Circe :: Dsl :: EmberServer :: Nil
  }

  object Sttp {
    val Version = "4.0.0-M17"

    val Cats  = "com.softwaremill.sttp.client4" %% "cats"          % Version
    val Circe = "com.softwaremill.sttp.client4" %% "circe"         % Version
    val Core  = "com.softwaremill.sttp.client4" %% "core"          % Version
    val Sl4j  = "com.softwaremill.sttp.client4" %% "slf4j-backend" % Version

    val All: List[ModuleID] = Cats :: Circe :: Core :: Sl4j :: Nil
  }

  object UnitTesting {
    final val ScalaTest  = "org.scalatest"     %% "scalatest"       % "3.2.18"
    final val ScalaCheck = "org.scalacheck"    %% "scalacheck"      % "1.18.1"
    final val ScalaMock  = "org.scalamock"     %% "scalamock"       % "6.0.0"
    final val Plus       = "org.scalatestplus" %% "scalacheck-1-16" % "3.2.14.0"

    final val All: List[ModuleID] =
      (
        Plus ::
          ScalaCheck ::
          ScalaMock ::
          ScalaTest ::
          Nil
      ).map(_ % Test)
  }
}
