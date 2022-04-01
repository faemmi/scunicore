package ai.mantik.executor.hpc.api.unicore.api

import ai.mantik.executor.hpc.testutils
import akka.util.ByteString
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.scaladsl.Source
import org.scalatest

import scala.concurrent.ExecutionContext

class FileSpec extends scalatest.FlatSpec with scalatest.Matchers {
  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  implicit val ec: ExecutionContext = akkaRuntime.executionContext
  implicit val mat = testutils.FakeAkkaRuntime.createMaterialier()

  val client = new testutils.api.http.FakeClient()
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
