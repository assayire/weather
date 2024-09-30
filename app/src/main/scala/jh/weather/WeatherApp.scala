package jh.weather

import cats.data.Kleisli
import cats.effect.*
import com.comcast.ip4s.{ipv4, port}
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import org.typelevel.log4cats.Logger

object WeatherApp extends IOApp.Simple with Logging[IO] {

  override def run: IO[Unit] =
    val routes = WeatherRoutes[IO]()

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(withErrorLogging(routes.apis))
      .build
      .useForever
      .as(ExitCode.Success)

  private def withErrorLogging(
    apis: HttpRoutes[IO]
  )(using Logger[IO]): Kleisli[IO, Request[IO], Response[IO]] =
    ErrorHandling.Recover.total(
      ErrorAction.log(
        apis.orNotFound,
        messageFailureLogAction = logError,
        serviceErrorLogAction   = logError
      )
    )

  private def logError(t: Throwable, msg: => String): IO[Unit] =
    IO.println(msg) >>
      IO.println(t) >>
      IO(t.printStackTrace(System.err))
}
