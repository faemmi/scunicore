package hpc.unicore.testutils.http

import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import akka.util.ByteString
import akka.stream.Materializer

object Streaming {
  val byteSink: Sink[ByteString, Future[ByteString]] = Sink.fold[ByteString, ByteString](ByteString())(_ ++ _)

  def materializeByteSource(
      source: Source[ByteString, Any]
  )(implicit materializer: Materializer): Future[ByteString] = {
    source.runWith(byteSink)
  }
}
