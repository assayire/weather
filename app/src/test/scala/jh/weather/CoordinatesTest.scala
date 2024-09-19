package jh.weather

import jh.weather.models.Coordinates
import org.scalacheck.Gen
import org.scalatest.Inside.inside
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

import scala.math.BigDecimal.RoundingMode

class CoordinatesTest extends AnyWordSpec with Matchers {
  private final val ValidLatitude  = 26
  private final val ValidLongitude = -70

  "Coordinates" must {
    "report error for invalid latitude value" in {
      val gen = Gen.choose(Double.MinValue, 24.4).map((_, ValidLongitude))

      forAll(gen) { (lat, lon) =>
        inside(Coordinates(lat, lon)) { case Left(e: IllegalArgumentException) =>
          e.getMessage must include("Invalid latitude")
        }
      }
    }

    "report error for invalid longitude value" in {
      val gen = Gen.choose(-66.91, Double.MaxValue).map((ValidLatitude, _))

      forAll(gen) { (lat, lon) =>
        inside(Coordinates(lat, lon)) { case Left(e: IllegalArgumentException) =>
          e.getMessage must include("Invalid longitude")
        }
      }
    }

    "create instance for valid values" in {
      val latRange =
        (
          BigDecimal(24.5d).setScale(2, RoundingMode.UP) to
            BigDecimal(71.5d).setScale(2, RoundingMode.UP) by
            1.0d
        ).map(_.doubleValue)

      val lonRange =
        (
          BigDecimal(-124.8).setScale(2, RoundingMode.UP) to
            BigDecimal(-66.9).setScale(2, RoundingMode.UP) by
            1.0d
        ).map(_.doubleValue)

      val coordinates =
        for {
          lat <- latRange
          lon <- lonRange
        } yield (lat, lon)

      org.scalatest.Inspectors.forAll(coordinates) { (lat, lon) =>
        Coordinates(lat, lon) must matchPattern { case Right(Coordinates(lat, lon)) =>
        }
      }
    }
  }

}
