package ai.mantik.executor.hpc.api.http

import ai.mantik.componently.{AkkaRuntime, ComponentBase}

import scala.concurrent.Future
import akka.http.scaladsl.model.{
  ContentType,
  ContentTypes,
  HttpEntity,
  HttpHeader,
  HttpMethod,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  RequestEntity,
  Uri,
  headers
}
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.NotUsed

import scala.collection.mutable.ListBuffer

trait ClientInterface {

  /** Send the request to the respective recipient. */
  def sendRequest(request: HttpRequest): Future[HttpResponse]

  /** Return the response as a [[Future]]. */
  def getResponse[A](response: Future[HttpResponse])(implicit unmarshaller: Unmarshaller[HttpResponse, A]): Future[A]

  /** Create a `GET` request.
    *
    * @param url URL to the endpoint of the API, relative to the base URL of the client.
    *            If blank (default), the base URL will be the target.
    * @param headers Headers to send with the request.
    * @param entity Body of the request.
    */
  def createGetRequest(
      url: String = "",
      headers: Seq[HttpHeader] = Seq(),
      entity: RequestEntity = HttpEntity.Empty
  ): HttpRequest =
    createRequest(HttpMethods.GET)(url = url, headers = headers, entity = entity)

  /** Create a `POST` request.
    *
    * @param url URL to the endpoint of the API, relative to the base URL of the client.
    *            If blank (default), the base URL will be the target.
    * @param headers Headers to send with the request.
    * @param entity Body of the request.
    */
  def createPostRequest(
      url: String = "",
      headers: Seq[HttpHeader] = Seq(),
      entity: RequestEntity = HttpEntity.Empty
  ): HttpRequest =
    createRequest(HttpMethods.POST)(url = url, headers = headers, entity = entity)

  /** Create a `PUT` request.
    *
    * @param url URL to the endpoint of the API, relative to the base URL of the client.
    *            If blank (default), the base URL will be the target.
    * @param headers Headers to send with the request.
    * @param entity Body of the request.
    */
  def createPutRequest(
      url: String = "",
      headers: Seq[HttpHeader] = Seq(),
      entity: RequestEntity = HttpEntity.Empty
  ): HttpRequest =
    createRequest(HttpMethods.PUT)(url = url, headers = headers, entity = entity)

  /** Create a `DELETE` request.
    *
    * @param url URL to the endpoint of the API, relative to the base URL of the client.
    *            If blank (default), the base URL will be the target.
    * @param headers Headers to send with the request.
    * @param entity Body of the request.
    */
  def createDeleteRequest(
      url: String = "",
      headers: Seq[HttpHeader] = Seq(),
      entity: RequestEntity = HttpEntity.Empty
  ): HttpRequest =
    createRequest(HttpMethods.DELETE)(url = url, headers = headers, entity = entity)

  /** Create the request for a certain HTTP method. */
  protected def createRequest[A <: HttpMethod](
      method: A
  )(url: String, headers: Seq[HttpHeader], entity: RequestEntity): HttpRequest
}

/** A REST client for interaction with a given API.
  *
  * @constructor Create the instance without any initial interaction with the API.
  * @param baseUrl Base URL of the API.
  */
class Client(
    val baseUrl: String,
    defaultHeaders: Seq[HttpHeader] = Seq(),
    authorizationHeader: Option[headers.Authorization] = None,
    acceptHeader: Option[headers.RawHeader] = None
)(
    implicit akkaRuntime: AkkaRuntime
) extends ComponentBase
    with ClientInterface {

  override def toString: String = s"${getClass.getName}(url = $baseUrl)"

  def withAuthenticationHeader(username: String, password: String): Client = {
    val newAuthorizationHeader = buildAuthenticationHeader(username, password)
    new Client(
      baseUrl = baseUrl,
      defaultHeaders = defaultHeaders,
      acceptHeader = acceptHeader,
      authorizationHeader = Some(newAuthorizationHeader)
    )
  }

  /** Re-instantiate with an `Accept: application/json` header. */
  def withJsonResponseHeader(): Client = withAcceptHeader(ContentTypes.`application/json`)

  /** Re-instantiate with an `Accept: application/octet-stream` header. */
  def withStreamResponseHeader(): Client = withAcceptHeader(ContentTypes.`application/octet-stream`)

  protected def withAcceptHeader(contentType: ContentType): Client = {
    val newAcceptHeader = headers.RawHeader("Accept", contentType.toString())
    new Client(
      baseUrl = baseUrl,
      defaultHeaders = defaultHeaders,
      acceptHeader = Some(newAcceptHeader),
      authorizationHeader = authorizationHeader
    )
  }

  /** Re-instantiate with a new base URL while keeping all headers. */
  def withNewBaseUrl(url: String): Client = new Client(
    baseUrl = url,
    defaultHeaders = defaultHeaders,
    acceptHeader = acceptHeader,
    authorizationHeader = authorizationHeader
  )

  /** Create a streamed request from a source. */
  def createStreamRequest(endpoint: String, source: Source[ByteString, Any]): HttpRequest = {
    val chunkedData = source.map(HttpEntity.ChunkStreamPart(_))
    val entity = HttpEntity.Chunked(
      contentType = ContentTypes.`application/octet-stream`,
      chunks = chunkedData
    )
    createPutRequest(url = endpoint, entity = entity)
  }

  /** Send a request and decode the request to a `String`. */
  def sendRequestAndDecodeResponseToString(endpoint: String = ""): Future[String] = {
    val request = createGetRequest(endpoint)
    val response = sendRequest(request)
    getResponse[String](response)(Unmarshallers.stringUnmarshaller)
  }

  protected def createRequest[A <: HttpMethod](method: A)(
      endpoint: String,
      headers: Seq[HttpHeader],
      entity: RequestEntity
  ): HttpRequest = {
    val updatedHeaders = addHeaders(headers)
    val target = if (endpoint.isBlank) baseUrl else buildEndpointUrl(endpoint)
    logger.debug(s"Building $method request for $target with body $entity")
    HttpRequest(
      method = method,
      uri = Uri(target),
      headers = updatedHeaders,
      entity = entity
    )
  }

  private def addHeaders(headers: Seq[HttpHeader]): Seq[HttpHeader] = {
    val allHeaders = ListBuffer[HttpHeader]()
    allHeaders.appendAll(headers)
    acceptHeader match {
      case Some(header) => allHeaders.append(header)
      case None         =>
    }
    authorizationHeader match {
      case Some(header) => allHeaders.append(header)
      case None         =>
    }
    allHeaders.toSeq
  }

  def sendRequest(request: HttpRequest): Future[HttpResponse] = {
    logger.debug(s"Sending request $request")
    Http().singleRequest(request)
  }

  def getResponse[A](
      response: Future[HttpResponse]
  )(implicit unmarshaller: Unmarshaller[HttpResponse, A]): Future[A] =
    Unmarshallers.unmarshal[HttpResponse, A](response)

  protected def buildEndpointUrl(endpoint: String): String = s"$baseUrl/$endpoint"

  protected def buildAuthenticationHeader(username: String, password: String): headers.Authorization =
    headers.Authorization(headers.BasicHttpCredentials(username, password))
}
