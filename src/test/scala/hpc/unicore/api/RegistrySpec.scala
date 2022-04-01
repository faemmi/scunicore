package hpc.unicore.api

import hpc.unicore.testutils
import akka.util.ByteString
import org.scalatest

import scala.concurrent.{ExecutionContext, Future}

class RegistrySpec extends scalatest.FlatSpec with scalatest.Matchers with scalatest.PrivateMethodTester {
  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  implicit val ec: ExecutionContext = akkaRuntime.executionContext
  val client = new testutils.api.http.FakeClient()
  val registry = new Registry(client)

  // Private methods.
  val CoreApiTypeVar = PrivateMethod[String](Symbol("CoreApiType"))
  val CoreApiType = Registry invokePrivate CoreApiTypeVar()

  val providesSiteApis = PrivateMethod[Future[Boolean]](Symbol("providesSiteApis"))
  val siteApiUrls = PrivateMethod[Future[Map[String, String]]](Symbol("siteApiUrls"))
  val getSiteApiUrls = PrivateMethod[List[String]](Symbol("getSiteApiUrls"))
  val getSiteNameFromApiUrl = PrivateMethod[String](Symbol("getSiteNameFromApiUrl"))

  "The registry" should "get all site URLs from a registry" in {
    def siteUrl(name: String): String = s"http://any-url:port/$name/rest/core"

    val site1 = "SITE1"
    val site1Url = siteUrl(site1)
    val site2 = "SITE2"
    val site2Url = siteUrl(site2)
    val response =
      s"""
         |{
         |  "owner":"test-owner",
         |  "currentTime":"2021-10-06T17:55:06+0200",
         |  "resourceStatus":"READY",
         |  "entries":[
         |    {
         |      "ServerIdentity":"identity1",
         |      "InterfaceNamespace":"https://www.unicore.eu/rest",
         |      "href":"$site1Url",
         |      "type":"$CoreApiType"
         |    },
         |    {
         |      "ServerIdentity":"identity3",
         |      "InterfaceNamespace":"https://www.unicore.eu/rest",
         |      "href":"should-not-be-included",
         |      "type":"StorageManagement"
         |    },
         |    {
         |      "ServerIdentity":"identity2",
         |      "InterfaceNamespace":"http://unigrids.org/2006/04/services/sms",
         |      "href":"$site2Url",
         |      "type":"$CoreApiType"
         |    }
         |  ],
         |  "resourceStatusMessage":"N/A",
         |  "siteName":"FZJ",
         |  "acl":[
         |  ],
         |  "tags":[
         |  ]
         |}
         |""".stripMargin
    client.respondsWith = ByteString(testutils.Json.multiLineStringToJson(response))
    val expected = Map(site1 -> site1Url, site2 -> site2Url)
    val resultFuture = registry invokePrivate siteApiUrls()
    val result = testutils.Concurrent.await(resultFuture)

    result should be(expected)

    val resultUrl1Future = registry.getSiteApiUrl(site1)
    val resultUrl1 = testutils.Concurrent.await(resultUrl1Future)
    val resultUrl2Future = registry.getSiteApiUrl(site2)
    val resultUrl2 = testutils.Concurrent.await(resultUrl2Future)

    resultUrl1 should be(site1Url)
    resultUrl2 should be(site2Url)
  }

  it should "catch if there are API URLs for sites" in {
    val entry = responses.Registry.Entry(
      ServerIdentity = "",
      InterfaceNamespace = "",
      href = "",
      apiType = ""
    )
    val reg = responses.Registry(
      owner = "",
      currentTime = "",
      resourceStatus = "",
      entries = List(entry),
      resourceStatusMessage = "",
      siteName = "",
      tags = List()
    )
    val method = registry invokePrivate providesSiteApis(testutils.Concurrent.asFuture(reg))
    val result = testutils.Concurrent.await(method)

    result should be(true)
  }

  it should "catch if there are no API URLs for any sites" in {
    val reg = responses.Registry(
      owner = "",
      currentTime = "",
      resourceStatus = "",
      entries = List(),
      resourceStatusMessage = "",
      siteName = "",
      tags = List()
    )
    val method = registry invokePrivate providesSiteApis(testutils.Concurrent.asFuture(reg))
    val result = testutils.Concurrent.await(method)

    result should be(false)
  }

  it should "get all site URLs from a response" in {
    val entries = List(
      responses.Registry.Entry(
        ServerIdentity = "",
        InterfaceNamespace = "",
        href = "test-api-url-1",
        apiType = CoreApiType
      ),
      responses.Registry.Entry(
        ServerIdentity = "",
        InterfaceNamespace = "",
        href = "test-api-url-2",
        apiType = CoreApiType
      ),
      responses.Registry.Entry(
        ServerIdentity = "",
        InterfaceNamespace = "",
        href = "test-api-url-3",
        apiType = "SomeOtherType"
      )
    )
    val result = Registry invokePrivate getSiteApiUrls(entries)

    result should be(List("test-api-url-1", "test-api-url-2"))
  }

  it should "get the site name from a URL" in {
    val url = "https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core"
    val result = Registry invokePrivate getSiteNameFromApiUrl(url)

    result should be("JUWELS")

    val url2 = "https://zam2125.zam.kfa-juelich.de:9112/rest/core"

    a[RuntimeException] should be thrownBy (Registry invokePrivate getSiteNameFromApiUrl(url2))
  }
}
