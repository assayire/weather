package jh.weather

import cats.MonadError
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO}
import jh.weather.models.nws.{GeoJson, Period}
import jh.weather.models.{Coordinates, Forecast}
import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import org.typelevel.log4cats.Logger

import java.time.ZonedDateTime

class WeatherSvcTest extends UnitTestSpec with MockFactory {
  "WeatherSvc APIs" must {
    "return forecast data for today" in new TestEnv() {
      private val coordinates = Coordinates(40.730610, -73.935242).value
      private val forecast    = periods()

      inSequence {
        mockQueryGeoJson(coordinates).once()
        mockQueryForecast(forecast).once()
      }

      val result: List[Forecast] =
        ws
          .forecastToday(coordinates)
          .unsafeRunSync()

      result must have length forecast.length
    }

    "return no data for today if NWS returns empty for today" in new TestEnv() {
      private val coordinates = Coordinates(40.730610, -73.935242).value
      private val forecast    = periods(ZonedDateTime.now.minusDays(4))

      inSequence {
        mockQueryGeoJson(coordinates).once()
        mockQueryForecast(forecast).once()
      }

      ws.forecastToday(coordinates).unsafeRunSync() mustBe empty
    }

    "return forecast now" in new TestEnv() {
      private val coordinates: Coordinates = Coordinates(39.7456, -97.0892).value

      inSequence {
        mockQueryGeoJson(coordinates).once()
        mockQueryForecastHourly(periods().head).once()
      }

      val result: List[Forecast] =
        ws
          .forecastNow(coordinates)
          .unsafeRunSync()

      result must have length 1
    }

    "report error if nws api returns error" in new TestEnv() {
      private val coordinates = Coordinates(41.75, -72.94).value
      private val ex          = NwsException("Failed to fetch forecast (400))")

      nwsMock.queryGeoJson
        .expects(*)
        .returning(IO.raiseError(ex))
        .once()

      intercept[NwsException](ws.forecastNow(coordinates).unsafeRunSync()) mustEqual ex
    }
  }

  private def periods(startTimeBegin: ZonedDateTime = ZonedDateTime.now) = {
    List(
      Period(
        name             = "This Afternoon",
        temperature      = 75,
        shortForecast    = "Mostly Sunny",
        detailedForecast = "Mostly sunny. Falls to 73 in the afternoon. Northeast wind around 9 mph.",
        startTime        = startTimeBegin,
        endTime          = startTimeBegin.plusHours(3),
        isDaytime        = true
      ),
      Period(
        name             = "Tonight",
        temperature      = 60,
        shortForecast    = "Partly Cloudy",
        detailedForecast = "Partly cloudy. Rises to 61 overnight. Northeast wind around 9 mph.",
        startTime        = startTimeBegin.plusHours(3),
        endTime          = startTimeBegin.plusHours(6),
        isDaytime        = false
      )
    )
  }

  /**
   * This class restricts the state to a single test
   * making it stateless; No two tests share state.
   * Order of tests or running in parallel does not
   * affect test behavior. I call it the TestEnv
   * pattern ¯\_(ツ)_/¯
   * 
   * The class also provides nice helpers that make
   * the test readable (free of setup boilerplate),
   * and sort of declarative.
   * 
   * NOTE: Should come back here (if time permits) to 
   * set arguments for expects in the mocks below. For 
   * now, anything goes because it is called after 
   * `queryGeoJson`.
   */
  private[WeatherSvcTest] class TestEnv(using
    val A:      Async[IO],
    val M:      MonadError[IO, Throwable],
    val logger: Logger[IO]
  ) {
    val nwsMock: NationalWeatherSvc[IO] = mock[NationalWeatherSvc[IO]]
    val ws:      WeatherSvc[IO]         = WeatherSvc[IO](nwsMock)

    def mockQueryGeoJson(cs: Coordinates): CallHandler1[Coordinates, IO[GeoJson]] =
      nwsMock.queryGeoJson
        .expects(cs)
        .returning(
          IO.pure(
            GeoJson(
              s"https://api.weather.gov/points/${cs.latitude},${cs.longitude}",
              "Feature",
              GeoJson.Properties(
                gridId         = "TOP",
                gridX          = 32,
                gridY          = 81,
                forecast       = "https://api.weather.gov/gridpoints/TOP/32,81/forecast",
                forecastHourly = "https://api.weather.gov/gridpoints/TOP/32,81/forecast/hourly"
              )
            )
          )
        )

    def mockQueryForecast(p: Period): CallHandler1[GeoJson, IO[List[Period]]] =
      nwsMock.queryForecast
        .expects(*)
        .returning(IO.pure(List(p)))

    def mockQueryForecast(ps: List[Period]): CallHandler1[GeoJson, IO[List[Period]]] =
      nwsMock.queryForecast
        .expects(*)
        .returning(IO.pure(ps))

    def mockQueryForecastHourly(p: Period): CallHandler1[GeoJson, IO[List[Period]]] =
      nwsMock.queryForecastHourly
        .expects(*)
        .returning(IO.pure(List(p)))
  }
}
