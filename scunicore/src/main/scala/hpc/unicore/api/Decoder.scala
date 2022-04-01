package ai.mantik.executor.hpc.api.unicore.api

import io.circe
import io.circe.parser

import scala.concurrent.{ExecutionContext, Future}

object Decoder {
  def decodeJsonString[A](
      json: Future[String]
  )(implicit decoder: circe.Decoder[A], ec: ExecutionContext): Future[A] = {
    json.map(x =>
      parser.decode[A](x) match {
        case Left(error)   => throw error
        case Right(result) => result
      }
    )
  }
}
