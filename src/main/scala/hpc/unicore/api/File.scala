package hpc.unicore.api

import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}
import hpc.unicore.http

class File(httpClient: http.Client)(implicit protected val ec: ExecutionContext)
    extends hpc.unicore.Component
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
  def fromUrl(url: String, httpClient: http.Client)(implicit ec: ExecutionContext): File = {
    val client = httpClient.withNewBaseUrl(url)
    new File(httpClient = client)
  }
}
