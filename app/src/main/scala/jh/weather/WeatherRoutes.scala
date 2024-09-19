package jh.weather

import cats.Monad
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import cats.effect.*
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.show.*
import io.circe.syntax.*
import jh.weather.models.Coordinates
import jh.weather.utils.{HomePage, given}
import org.http4s.*
import org.http4s.Status.BadRequest
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.*
import org.http4s.headers.`Content-Type`
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

class WeatherRoutes[F[_]: Monad: Async](using log: Logger[F]) {
  val apis: HttpRoutes[F] =
    Router(
      "/api" -> weatherRoutes,
      "/dxg" -> sampleRoutes,
      "/"    -> homeRoute
    )

  def weatherRoutes: HttpRoutes[F] =
    val dsl = Http4sDsl[F]
    import dsl.*

    val ws = WeatherSvc[F]()

    HttpRoutes.of[F] {
      case req @ GET -> Root / "weather" / "now" :? Coordinates.QueryParamMatcher(parsed) =>
        log
          .info(s"Received request: ${req.uri.renderString} ...")
          .map(_ => parsed)
          .flatMap {
            case Valid(cs) =>
              Ok(
                ws
                  .forecastNow(cs)
                  .flatTap {
                    case List() => log.warn(s"Current forecast data not available for $cs!")
                    case _      => ().pure[F]
                  }
                  .map(_.asJson)
              )

            case Invalid(fs) =>
              log
                .info(fs.toList.show)
                .flatMap(_ => BadRequest().map(_.withEntity(fs.head.message)))
          }

      case req @ GET -> Root / "weather" :? Coordinates.QueryParamMatcher(parsed) =>
        log
          .info(s"Received request: ${req.uri.renderString} ...")
          .map(_ => parsed)
          .flatMap {
            case Valid(cs) =>
              Ok(
                ws
                  .forecastToday(cs)
                  .flatTap {
                    case List() => log.warn(s"No forecast data available for the day for $cs!")
                    case _      => ().pure[F]
                  }
                  .map(_.asJson)
                  .map(_.asJson)
              )
            case Invalid(fs) =>
              log
                .info(fs.toList.show)
                .flatMap(_ => BadRequest().map(_.withEntity(fs.head.message)))
          }
    }

  def sampleRoutes: HttpRoutes[F] =
    val dsl = Http4sDsl[F]
    import dsl.*

    HttpRoutes.of[F] { case GET -> Root / "hello" / name =>
      Ok(
        summon[Logger[F]]
          .info(s"[dxg] /hello/$name")
          .map(_ => s"Hello, $name.")
      )
    }

  private def homeRoute: HttpRoutes[F] =
    val dsl = Http4sDsl[F]
    import dsl.*

    HttpRoutes.of[F] { case req @ GET -> Root =>
      log
        .info(s"Service home page ...")
        .flatMap { _ =>
          Response(
            status  = Status.Ok,
            headers = Headers(`Content-Type`(MediaType.text.html)),
            body    = fs2.Stream.emits(HomePage().getBytes)
          ).pure[F]
        }
    }

}
