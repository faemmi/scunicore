package ai.mantik.executor.hpc.api.unicore.api

import ai.mantik.componently.{AkkaRuntime, ComponentBase}
import ai.mantik.executor.hpc.api.http
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.Future

/** Working directory of a job. */
class WorkingDirectory(httpClient: http.Client)(implicit akkaRuntime: AkkaRuntime)
    extends ComponentBase
    with Resource[responses.WorkingDirectory] {
  protected val client: http.Client = httpClient

  override def toString: String = s"${getClass.getName}(url = ${client.baseUrl})"

  /** All links available for the directory. */
  def links(): Future[responses.WorkingDirectory.Links] = properties().map(_.links)

  /** Properties of the directory (API response). */
  def properties(): Future[responses.WorkingDirectory] = {
    logger.debug(s"Getting properties for $this")
    val response = client.sendRequestAndDecodeResponseToString()
    Decoder.decodeJsonString[responses.WorkingDirectory](response)
  }

  /** Upload a file to the directory. */
  def uploadFile(file: String, source: Source[ByteString, Any]): Future[akka.http.scaladsl.model.HttpResponse] = {
    logger.debug(s"Uploading file $file to $this")
    val path = s"${Endpoints.WorkingDirectory.files}/$file"
    val request = client.createStreamRequest(
      endpoint = path,
      source = source
    )
    client.sendRequest(request)
  }

  /** Get all files in the directory. */
  def getFiles(): Future[responses.Path] = {
    logger.debug(s"Getting all files in $this")
    getPath(Endpoints.WorkingDirectory.files)
  }

  /** Get a single file in the directory. */
  def getFile(file: String): Future[File] = {
    logger.debug(s"Getting file $file from $this")
    val path = s"${Endpoints.WorkingDirectory.files}/$file"
    val fileProperties = getPath(path)
    fileProperties.map(p => {
      if (p.isDirectory) {
        throw new RuntimeException(s"$file is a directory")
      } else {
        val fileUrl = s"${client.baseUrl}/$path"
        File.fromUrl(fileUrl, httpClient = client)
      }
    })
  }

  private def getPath(path: String): Future[responses.Path] = {
    val response = client.sendRequestAndDecodeResponseToString(path)
    Decoder.decodeJsonString[responses.Path](response)
  }

}

object WorkingDirectory {
  def fromUrl(url: String, httpClient: http.Client)(implicit akkaRuntime: AkkaRuntime): WorkingDirectory = {
    val client = httpClient.withNewBaseUrl(url)
    new WorkingDirectory(httpClient = client)
  }
}
