package jh.weather

import cats.Show
import org.http4s.ParseFailure
import sttp.client4.Response

import scala.io.Source
import scala.util.Using

package object utils:
  object RequestFailed:
    def unapply[L, R](res: Response[Either[L, R]]): Option[Response[Either[L, R]]] =
      if res.code.isSuccess then None else Some(res)

  given Show[List[ParseFailure]] with
    override def show(fs: List[ParseFailure]): String =
      val es = fs.map(_.sanitized).mkString(", ")
      s"[ParseFailure(s)] $es"

  def resourceAsString(name: String): String =
    Using.resource(WeatherApp.getClass.getClassLoader.getResourceAsStream(name)) { stream =>
      Source.fromInputStream(stream).mkString
    }
