package ai.mantik.executor.hpc.api.http

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import io.circe.{Json, JsonObject}

object Unmarshallers {

  def unmarshal[A, B](
      value: Future[A]
  )(implicit unmarshaller: Unmarshaller[A, B], context: ExecutionContext, materialzer: Materializer): Future[B] =
    value.flatMap(x => Unmarshal(x).to[B])

  implicit val jsonUnmarshaller: Unmarshaller[HttpResponse, Json] =
    baseJsonUnmarshaller[Json](json => json)

  implicit val jsonObjectUnmarshaller: Unmarshaller[HttpResponse, JsonObject] =
    baseJsonUnmarshaller[JsonObject](json =>
      json.asObject match {
        case Some(value) => value
        case None        => throw new RuntimeException(s"$json not a valid JSON object")
      }
    )

  private def baseJsonUnmarshaller[A](jsonResultHandler: Json => A): Unmarshaller[HttpResponse, A] =
    new Unmarshaller[HttpResponse, A] {
      override def apply(
          value: HttpResponse
      )(implicit context: ExecutionContext, materializer: Materializer): Future[A] = {
        val collector = { (x: ByteString) =>
          io.circe.jawn.parseByteBuffer(x.asByteBuffer) match {
            case Left(error) => Future.failed(new RuntimeException(s"Could not parse JSON: $error"))
            case Right(ok)   => Future.successful(jsonResultHandler(ok))
          }
        }
        collectSource[A](value)(collector)
      }
    }

  implicit val stringUnmarshaller: Unmarshaller[HttpResponse, String] = new Unmarshaller[HttpResponse, String] {
    override def apply(
        value: HttpResponse
    )(implicit context: ExecutionContext, materializer: Materializer): Future[String] = {
      val collector = (x: ByteString) => Future { x.utf8String }
      collectSource[String](value)(collector)
    }
  }

  private def collectSource[A](response: HttpResponse)(collector: ByteString => Future[A])(
      implicit context: ExecutionContext,
      materializer: Materializer
  ): Future[A] = {
    val source = response.entity.dataBytes
    source.runWith(Sink.seq[ByteString]).flatMap { collected: Seq[ByteString] =>
      val combined = collected.foldLeft(ByteString.empty)(_ ++ _)
      collector(combined)
    }
  }
}
