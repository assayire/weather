package jh.weather.models

opaque type Latitude = Double

object Latitude:
  def apply(value: Double): Option[Latitude] =
    if value >= 24.5 && value <= 71.5 then Some(value)
    else None

  def apply(value: String): Option[Latitude] =
    if value.isBlank then None
    else value.toDoubleOption.flatMap(apply)

  /**
   * Took the valid range of values below
   * from internet search. Don't mind if it
   * is not accurate.
   */
  def unsafe(value: Double): Latitude =
    apply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid latitude value: $value")
    }

  extension (lat: Latitude) def value: Double = lat
