package jh.weather

import jh.weather.models.Longitude
import org.scalacheck.{Gen, Shrink}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

import scala.math.BigDecimal.RoundingMode

class LongitudeTest extends AnyWordSpec with Matchers {
  "Longitude" must {
    "report error for lower out of range double value(s)" in {
      forAll(Gen.choose(Double.MinValue, -124.7)) { d =>
        Longitude(d) mustBe empty
        Longitude(d.toString) mustBe empty
      }
    }
  }

  "report error for upper out of range double value(s)" in {
    forAll(Gen.choose(-66.91, Double.MaxValue)) { d =>
      Longitude(d) mustBe empty
      Longitude(d.toString) mustBe empty
    }
  }

  "create instance for valid value" in {
    val ds =
      (
        BigDecimal(-124.8).setScale(2, RoundingMode.UP) to
          BigDecimal(-66.9).setScale(2, RoundingMode.UP) by
          1.0d
      ).map(_.doubleValue)

    org.scalatest.Inspectors.forAll(ds) { d =>
      Longitude(d).map(_.value) must contain(d)
    }
  }
}
