package jh.weather.models.nws

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

/**
 * Represents selective parts of the response body for:
 * https://api.weather.gov/points/$latitude%2C$longitude
 * 
 * We are not parsing the entire humongous json as we
 * need only a few fields for our weather service app.
 */
case class GeoJson(
  id:         String,
  `type`:     String,
  properties: GeoJson.Properties
)

object GeoJson:
  given encoder: Encoder[GeoJson] = deriveEncoder
  given decoder: Decoder[GeoJson] = deriveDecoder

  case class Properties(
    gridId:         String,
    gridX:          Int,
    gridY:          Int,
    forecast:       String,
    forecastHourly: String
  )

  object Properties:
    given encoder: Encoder[Properties] = deriveEncoder
    given decoder: Decoder[Properties] = deriveDecoder
