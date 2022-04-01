package ai.mantik.executor.hpc.api.unicore.api

import ai.mantik.executor.hpc.testutils
import ai.mantik.executor.hpc.api.http
import org.scalatest

import scala.concurrent.{ExecutionContext, Future}

class RegistryItSpec extends scalatest.FlatSpec with scalatest.Matchers with scalatest.PrivateMethodTester {
  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  implicit val ec: ExecutionContext = akkaRuntime.executionContext
  val registryUrl = "https://fzj-unic.fz-juelich.de:9112/FZJ/rest/registries/default_registry"
  val httpClient = new http.Client(baseUrl = registryUrl)
  val registry = new Registry(httpClient)

  // Private methods.
  val siteApiUrls = PrivateMethod[Future[Map[String, String]]](Symbol("siteApiUrls"))

  "The registry" should "get all site URLs from a registry" in {
    val resultFuture = registry invokePrivate siteApiUrls()
    val result = testutils.Concurrent.await(resultFuture, maxWaitTime = 30)

    result.nonEmpty should be(true)

  }

  it should "get the site API URL for JUWELS" in {
    val site = "JUWELS"
    val resultFuture = registry.getSiteApiUrl(site)
    val result = testutils.Concurrent.await(resultFuture, maxWaitTime = 30)

    result.contains(site) should be(true)
  }
}
