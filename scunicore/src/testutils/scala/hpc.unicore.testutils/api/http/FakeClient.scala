package ai.mantik.executor.hpc.testutils.api.http

import ai.mantik.componently

import scala.concurrent.Future
import akka.http.scaladsl.model.{
  ContentType,
  HttpEntity,
  HttpHeader,
  HttpRequest,
  HttpResponse,
  RequestEntity,
  ResponseEntity,
  StatusCode,
  StatusCodes
}
import akka.util.ByteString
import ai.mantik.executor.hpc.api.http

import scala.collection.mutable.ListBuffer

class FakeClient(implicit akkaRuntime: componently.AkkaRuntime) extends http.Client("") {
  var respondsWithEntity: Option[ResponseEntity] = None
  var respondsWith: ByteString = ByteString("")
  var responseStatus: StatusCode = StatusCodes.OK
  var responseHeaders: Seq[HttpHeader] = Seq()
  var requestsSent: ListBuffer[HttpRequest] = ListBuffer[HttpRequest]()

  override def withAuthenticationHeader(username: String, password: String): FakeClient = this

  override def withJsonResponseHeader(): FakeClient = this

  override def withStreamResponseHeader(): FakeClient = this

  override def withAcceptHeader(contentType: ContentType): FakeClient = this

  override def withNewBaseUrl(url: String): FakeClient = this

  override def createGetRequest(url: String, headers: Seq[HttpHeader], entity: RequestEntity): HttpRequest =
    HttpRequest()

  override def sendRequest(request: HttpRequest): Future[HttpResponse] = {
    logger.debug(s"Sending request $request")
    requestsSent.append(request)
    val entity = respondsWithEntity match {
      case Some(entity) => entity
      case None         => HttpEntity(respondsWith)
    }
    Future { HttpResponse(status = responseStatus, headers = responseHeaders, entity = entity) }
  }

}
