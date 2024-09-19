package jh.weather

import cats.effect.IO
import org.scalamock.scalatest.MockFactory
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.temporal.ChronoUnit.DAYS
import java.time.ZonedDateTime

trait UnitTestSpec
    extends AnyWordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with MockFactory
    with Logging[IO] {

  def startOfDay: ZonedDateTime = ZonedDateTime.now.truncatedTo(DAYS)

  def midDay: ZonedDateTime =
    ZonedDateTime.now
      .withHour(12)
      .withMinute(0)
      .withSecond(0)
      .withNano(0)
}
