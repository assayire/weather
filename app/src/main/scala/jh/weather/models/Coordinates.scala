package jh.weather.models

import cats.syntax.either.*
import jh.weather.common.ErrorOr
import org.http4s.dsl.io.{QueryParamDecoderMatcher, ValidatingQueryParamDecoderMatcher}
import org.http4s.{ParseResult, QueryParamDecoder}

case class Coordinates(latitude: Latitude, longitude: Longitude)

object Coordinates:
  private val InvalidLatitude    = "Invalid latitude"
  private val InvalidLongitude   = "Invalid longitude"
  private val InvalidCoordinates = "Invalid co-ordinates"

  given queryParamDecoder: QueryParamDecoder[Coordinates] =
    QueryParamDecoder[String]
      .emap { value =>
        parseRaw(value).leftFlatMap(t => ParseResult.fail(t.getMessage, ""))
      }

  // ValidatingQueryParamDecoderMatcher
  // object QueryParamMatcher extends QueryParamDecoderMatcher[Coordinates]("c")
  object QueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Coordinates]("c")

  def apply(lat: Double, lon: Double): ErrorOr[Coordinates] =
    (
      for {
        l1 <- Latitude(lat).toRight("Invalid latitude")
        l2 <- Longitude(lon).toRight("Invalid longitude")
      } yield Coordinates(l1, l2)
    ).leftMap(e => new IllegalArgumentException(e))

  /**
   * Helper method to parse the query parameter value
   */
  private def parseRaw(raw: String): ErrorOr[Coordinates] =
    parseDoubles(raw) match
      case Left(err)         => ParseResult.fail(err, "")
      case Right((lat, lon)) => Coordinates(lat, lon)

  private def parseDoubles(raw: String): Either[String, (Double, Double)] =
    if raw.isBlank then Left("Invalid value: empty")
    else {
      raw.split(",") match
        case Array(l1, l2) =>
          for {
            lat <- l1.toDoubleOption.toRight("Invalid format: latitude")
            lon <- l2.toDoubleOption.toRight("Invalid format: longitude")
          } yield (lat, lon)
        case _ => Left("Invalid format")
    }
