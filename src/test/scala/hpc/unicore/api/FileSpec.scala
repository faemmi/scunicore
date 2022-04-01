package hpc.unicore.api

import hpc.unicore.testutils
import akka.util.ByteString
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.scaladsl.Source
import org.scalatest

import scala.concurrent.ExecutionContext

class FileSpec extends scalatest.FlatSpec with scalatest.Matchers {
  implicit val actor: akka.actor.ActorSystem = akka.actor.ActorSystem()
  implicit val ec: ExecutionContext = ExecutionContext.global

  val client = new testutils.http.FakeClient()
  val file = new File(client)

  "A file" should "be downloadable" in {
    val content = ByteString("Test")
    val responseContent = Source.single(content)
    client.respondsWithEntity =
      Some(HttpEntity.Chunked.fromData(ContentTypes.`application/octet-stream`, responseContent))
    val source = file.download()
    val res = source.flatMap(testutils.api.http.Streaming.materializeByteSource)

    val result = testutils.Concurrent.await(res)

    result should be(content)
  }
}
