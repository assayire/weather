package jh.weather.models

import cats.effect.IO
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import jh.weather.models.nws.Period
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class Forecast(
  temperature:               Int,
  shortForecast:             String,
  detailedForecast:          String,
  temperateCharacterization: String,
  isDaytime:                 Boolean
)

object Forecast:
  given encoder: Encoder[Forecast] = deriveEncoder
  given decoder: Decoder[Forecast] = deriveDecoder

  given http4sEncoder: EntityEncoder[IO, Forecast] = jsonEncoderOf[IO, Forecast]
  given http4sDecoder: EntityDecoder[IO, Forecast] = jsonOf[IO, Forecast]

  /**
   * Temperature evaluated assuming it is in Fahrenheit.
   * But the evaluation is totally arbitrary, and is not
   * based on personal preference :)
   */
  def evalTemperature(temp: Int): String =
    if temp >= 96 then "Burning"
    else if temp >= 80 && temp <= 95 then "Hot"
    else if temp >= 65 && temp <= 80 then "Warm"  // As in very nice with goggles on!
    else if temp >= 50 && temp <= 64 then "Chill" // As in nice. Not chilly :)
    else if temp >= 10 && temp <= 49 then "Cold"
    else "Freezing"

  given Conversion[Period, Forecast] with
    override def apply(p: Period): Forecast = {
      Forecast(
        p.temperature,
        p.shortForecast,
        p.detailedForecast,
        evalTemperature(p.temperature),
        p.isDaytime
      )
    }
