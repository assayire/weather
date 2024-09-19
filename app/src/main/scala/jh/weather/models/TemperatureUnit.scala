package jh.weather.models

import io.circe.{Decoder, Encoder}
import io.circe.syntax.*

import scala.util.{Success, Failure}

enum TemperatureUnit(val code: String):
  case Fahrenheit extends TemperatureUnit("F")
  case Celsius extends TemperatureUnit("C")

object TemperatureUnit:
  given encoder: Encoder[TemperatureUnit] = Encoder.instance(_.code.asJson)

  given decoder: Decoder[TemperatureUnit] = Decoder.instanceTry { cur =>
    cur.value.asString.flatMap(of) match
      case Some(t) => Success(t)
      case None    => Failure(new IllegalArgumentException(s"Invalid temperature unit"))
  }

  def apply(value: String): TemperatureUnit =
    of(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid temperature unit: $value")
    }

  def of(raw: String): Option[TemperatureUnit] =
    TemperatureUnit.values.find(t => t.toString == raw || t.code == raw)
