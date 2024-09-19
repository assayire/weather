package jh.weather.models

opaque type Longitude = Double

object Longitude:
  def apply(value: Double): Option[Longitude] =
    if value >= -124.8 && value <= -66.9 then Some(value)
    else None

  def apply(value: String): Option[Longitude] =
    if value.isBlank then None
    else value.toDoubleOption.flatMap(apply)

  /**
   * Took the valid range of values below
   * from internet search. Don't mind if it
   * is not accurate.
   */
  def unsafeApply(value: Double): Longitude =
    apply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid longitude value: $value")
    }

  extension (lon: Longitude) def value: Double = lon
