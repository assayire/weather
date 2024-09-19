package jh.weather

import jh.weather.models.Latitude
import org.scalacheck.Gen
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

import scala.math.BigDecimal.RoundingMode

class LatitudeTest extends AnyWordSpec with Matchers {
  "Latitude" must {
    "report error for lower out of range double value(s)" in {
      forAll(Gen.choose(Double.MinValue, 24.4)) { d =>
        Latitude(d) mustBe empty
        Latitude(d.toString) mustBe empty
      }
    }
  }

  "report error for upper out of range double value(s)" in {
    forAll(Gen.choose(71.6, Double.MaxValue)) { d =>
      Latitude(d) mustBe empty
      Latitude(d.toString) mustBe empty
    }
  }

  "create instance for valid value" in {
    val ds =
      (
        BigDecimal(24.5d).setScale(2, RoundingMode.UP) to
          BigDecimal(71.5d).setScale(2, RoundingMode.UP) by
          1.0d
      ).map(_.doubleValue)

    org.scalatest.Inspectors.forAll(ds) { d =>
      Latitude(d).map(_.value) must contain(d)
    }
  }
}
