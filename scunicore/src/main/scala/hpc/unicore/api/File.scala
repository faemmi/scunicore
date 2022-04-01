package ai.mantik.executor.hpc.api.unicore.api

import ai.mantik.componently.{AkkaRuntime, ComponentBase}
import ai.mantik.executor.hpc.api.http
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.Future

class File(httpClient: http.Client)(implicit akkaRuntime: AkkaRuntime)
    extends ComponentBase
    with Resource[responses.Path] {
  protected val client: http.Client = httpClient

  override def toString: String = s"${getClass.getName}(url = ${client.baseUrl})"

  override def properties(): Future[responses.Path] = {
    logger.debug(s"Getting properties for $this")
    val response = client.sendRequestAndDecodeResponseToString()
    Decoder.decodeJsonString[responses.Path](response)
  }

  /** Download the file. */
  def download(): Future[Source[ByteString, Any]] = {
    logger.debug(s"Downloading file $this")
    val streamingClient = client.withStreamResponseHeader()
    val request = streamingClient.createGetRequest()
    val response = streamingClient.sendRequest(request)
    response.map(_.entity.dataBytes)
  }

}
object File {
  def fromUrl(url: String, httpClient: http.Client)(implicit akkaRuntime: AkkaRuntime): File = {
    val client = httpClient.withNewBaseUrl(url)
    new File(httpClient = client)
  }
}
