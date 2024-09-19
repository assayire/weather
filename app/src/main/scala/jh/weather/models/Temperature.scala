package jh.weather.models

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

case class Temperature(value: Double, unit: TemperatureUnit) {
  val characterization: String = "hot"
}

object Temperature:
  given encoder: Encoder[Temperature] = deriveEncoder
  given decoder: Decoder[Temperature] = deriveDecoder

  def apply(value: Double, unitStr: String): Temperature =
    new Temperature(
      value,
      // Assuming that NWS will never return invalid values
      TemperatureUnit(unitStr)
    )
