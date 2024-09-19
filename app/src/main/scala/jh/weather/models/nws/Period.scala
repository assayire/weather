package jh.weather.models.nws

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.ZonedDateTime

/**
 * This class denotes the root/properties/periods[x]
 * element in the response payload for the NWS 
 * forecast url.
 * 
 * - Temperature unit is assumed to be Fahrenheit
 * - Looks like NWS will not return a `detailedForecast`
 *   in an hourly forecast response. We should be okay
 *   as long as we have a `shortForecast`.
 */
case class Period(
  name:             String,
  temperature:      Int,
  shortForecast:    String,
  detailedForecast: String,
  startTime:        ZonedDateTime,
  endTime:          ZonedDateTime,
  isDaytime:        Boolean
)

object Period:
  given decoder: Decoder[Period] = deriveDecoder
  given encoder: Encoder[Period] = deriveEncoder
