package jh.weather

import cats.effect.Async
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.{Monad, MonadError}
import jh.weather.models.Forecast.given
import jh.weather.models.nws.Period
import jh.weather.models.{Coordinates, Forecast}
import org.typelevel.log4cats.Logger

import java.time.LocalDate

/**
 * This class talks to the [[Nws]] service, and exposes
 * APIs returning data to cater to our problem exercise.
 *
 * NOTES:
 * - It appears NWS might not return forecast or forecast
 *   hourly data for the current date if requested some time
 *   late in the night (but still on the same date). I don't
 *   fully understand their behavior as it is not documented
 *   AFAIK. I am going with the tradeoff that we return no
 *   data from our service if NWS does not return any data
 *   for the current date. This also impacts the unit test
 *   for the class (if it is run in the late nighttime). The
 *   hard part is that the scenario can be realized only during
 *   late nighttime. So, I am guessing the tradeoff is alright
 *   for the moment. If I get to understand their API behavior,
 *   our API can be fixed accordingly.
 * - The other part with empty forecast data is that we could
 *   probably cache the data for the date and location. I am not
 *   particularly thrilled with an in-memory cache since it
 *   would be thrown away when the application quits. So, it has 
 *   got to be a persistence store. I have mixed feelings about 
 *   using a file based store. Because managing the store is a
 *   bit of hassle. Last option is a database. I wasn't sure if
 *   I should go as far as integrating with a database for this 
 *   exercise. It can totally be done but delaying to on-demand
 *   (if you want me to). So, the empty forecast data mentioned
 *   in the above point is the trade-off.
 *   
 * I know, I know, I could have written some code to cache the 
 * data instead of writing this big fat comment ^_^.
 */
class WeatherSvc[F[_]: Monad] private[weather] (private val nws: NationalWeatherSvc[F])(using
  val A:      Async[F],
  val M:      MonadError[F, Throwable],
  val logger: Logger[F]
) {
  def this()(using
    A:      Async[F],
    M:      MonadError[F, Throwable],
    logger: Logger[F]
  ) = this(new Nws[F]())

  /**
   * This method gives a single and current forecast datum.
   * 
   * NOTE: This function returns empty list if NWS does not
   * return forecast data for the day. See the big fat comment
   * above in [[WeatherSvc]].
   */
  def forecastNow(c: Coordinates): F[List[Forecast]] =
    for
      g  <- nws.queryGeoJson(c)
      fs <- nws.queryForecastHourly(g)
    yield fs match
      case List()          => List.empty
      case List(first, _*) => List(first)

  /**
   * This function gives the forecast data for the current day. Based on
   * the explanation in [[Nws]], this function should return more than
   * one element in the list; 2 most of the time.
   * 
   * NOTE: This function returns empty list if NWS does not
   * return forecast data for the day. See the big fat comment
   * above in [[WeatherSvc]].
   */
  def forecastToday(c: Coordinates): F[List[Forecast]] =
    (
      for
        g  <- nws.queryGeoJson(c)
        fs <- nws.queryForecast(g)
      yield fs.filter(_.startTime.toLocalDate == LocalDate.now)
    ).map(_.map(_.convert))
}
