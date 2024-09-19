package jh.weather

package object common:
  type ErrorOr[A] = Either[Throwable, A]
