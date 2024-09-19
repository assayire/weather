package jh.weather

import cats.effect.{IO, Sync}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{Logger, LoggerFactory, LoggerName}

trait Logging[F[_]: Sync] {
  given logFactory: LoggerFactory[F] = Slf4jFactory.create[F]
  given logger:     Logger[F]        = logFactory.getLogger(LoggerName(getClass.getTypeName))
}

object Logging extends Logging[IO]
