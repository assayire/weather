package jh.weather

import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.{Monad, MonadError}
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.parser.*
import jh.weather.models.*
import jh.weather.models.nws.{GeoJson, Period}
import jh.weather.utils.*
import org.typelevel.log4cats.Logger
import sttp.client4.*
import sttp.client4.httpclient.cats.HttpClientCatsBackend
import sttp.client4.logging.slf4j.Slf4jLoggingBackend

import java.net.http.HttpClient as JHttpClient
import java.time.Duration as JDuration

trait NationalWeatherSvc[F[_]]:
  def queryGeoJson(c:        Coordinates): F[GeoJson]
  def queryForecast(g:       GeoJson):     F[List[Period]]
  def queryForecastHourly(g: GeoJson):     F[List[Period]]

/**
 * This class talks to the NWS APIs to query the forecast data,
 * and return the response as-is without data modifications;
 * although we are extracting selective/partial parts of the
 * response for brevity to cater to the given problem.
 *
 * NWS API Information:
 * - We first call the geo json end point with the (latitude, longitude)
 *   and the response payload contains the forecast url.
 * - We extract the forecast url and make a call.
 *   - The response payload contains a root/properties/periods node, 
 *     which is our element of interest for our exercise. We are 
 *     ignoring rest of the payload for brevity.
 *   - My understanding is that periods contains two objects per day.
 *     Upstream logic is based on this understanding. We are parsing
 *     only selective items in this object; only those are necessary
 *     for our exercise.
 *   - We are assuming the temperature unit is always Fahrenheit
 */
class Nws[F[_]: Monad](using
  val A:      Async[F],
  val M:      MonadError[F, Throwable],
  val logger: Logger[F]
) extends NationalWeatherSvc[F] {

  private val httpClient: JHttpClient =
    JHttpClient
      .newBuilder()
      .connectTimeout(JDuration.ofSeconds(20))
      .followRedirects(JHttpClient.Redirect.NORMAL)
      .build()

  override def queryGeoJson(c: Coordinates): F[GeoJson] =
    logger
      .info("NWS: Querying Geo Json ...")
      .flatMap { _ =>
        execSttpRequest(basicRequest.get(geoJsonUri(c)).send(_))
          .flatMap {
            case RequestFailed(r) =>
              M.raiseError(NwsException(s"Failed to fetch geoJson (${r.code})"))
            case res =>
              res.body
                .leftMap(NwsException(_))
                .flatMap(parse)
                .flatMap(_.as[GeoJson])
                .leftMap(NwsException(_))
                .fold(M.raiseError, M.pure)
          }
      }

  override def queryForecast(g: GeoJson): F[List[Period]] =
    logger
      .info("NWS: Querying forecast ...")
      .flatMap { _ =>
        execSttpRequest(basicRequest.get(uri"${g.properties.forecast}").send(_))
          .flatMap {
            case RequestFailed(r) =>
              M.raiseError(NwsException(s"Failed to fetch forecast (${r.code})"))
            case res =>
              res.body
                .leftMap(NwsException(_))
                .flatMap(parse)
                .flatMap(extractForecastData)
                .leftMap(NwsException(_))
                .fold(M.raiseError, M.pure)
          }
      }

  override def queryForecastHourly(g: GeoJson): F[List[Period]] =
    logger
      .info("NWS: Querying hourly forecast ...")
      .flatMap { _ =>
        execSttpRequest(basicRequest.get(uri"${g.properties.forecastHourly}").send(_))
          .flatMap {
            case RequestFailed(r) =>
              M.raiseError(NwsException(s"Failed to fetch forecast (${r.code})"))
            case res =>
              res.body
                .leftMap(NwsException(_))
                .flatMap {
                  parse(_)
                    .flatMap(extractForecastData)
                    .leftMap(NwsException(_))
                }
                .fold(M.raiseError, M.pure)
          }
      }

  /**
   * Limiting extraction to only the temperature/forecast 
   * data. Other parts ignored for brevity of this exercise.
   */
  private def extractForecastData(json: Json): Result[List[Period]] =
    json.hcursor
      .downField("properties")
      .downField("periods")
      .as[List[Period]]

  private def execSttpRequest(
    fn: Backend[F] => F[Response[Either[String, String]]]
  ): F[Response[Either[String, String]]] =
    Dispatcher.parallel.use { dispatcher =>
      val backend = HttpClientCatsBackend.usingClient(httpClient, dispatcher)
      fn(Slf4jLoggingBackend(backend))
    }

  private def geoJsonUri(c: Coordinates) =
    uri"https://api.weather.gov/points/${c.latitude}%2C${c.longitude}"
}

class NwsException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(cause: Throwable) = this(cause.getMessage, cause)
  def this(msg:   String)    = this(msg, None.orNull)
}
