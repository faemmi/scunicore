package hpc.unicore.api.http

import akka.http.scaladsl.model.{HttpEntity, HttpRequest, RequestEntity, headers}
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

import hpc.unicore.api.http
import hpc.unicore.testutils

class RestClientSuite extends FlatSpec with Matchers with PrivateMethodTester {

  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  val client = new http.Client(baseUrl = "test-url")

  val buildEndpointUrl = PrivateMethod[String](Symbol("buildEndpointUrl"))
  val buildAuthenticationHeader = PrivateMethod[headers.Authorization](Symbol("buildAuthenticationHeader"))

  "The REST client" should "generate the endpoint URL" in {
    val result = client invokePrivate buildEndpointUrl("test-endpoint")

    result should be("test-url/test-endpoint")
  }

  it should "build an authentication header" in {
    val header = client invokePrivate buildAuthenticationHeader("test-user", "test-password")
    val encodedCredentials: String = "dGVzdC11c2VyOnRlc3QtcGFzc3dvcmQ="

    header.credentials.token should be(encodedCredentials)
  }

  it should "build a request" in {
    val header = client invokePrivate buildAuthenticationHeader("test-user", "test-password")
    val request = client.createGetRequest(
      url = "test-url",
      headers = Seq(header),
      entity = HttpEntity("test-data").asInstanceOf[RequestEntity]
    )

    request.isInstanceOf[HttpRequest] should be(true)
  }
}