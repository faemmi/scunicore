package hpc.unicore.api.unicore

import org.scalatest
import hpc.unicore.api.http
import hpc.unicore.{HpcExecutorConfig, testutils}
import akka.util.ByteString
import com.typesafe

import scala.concurrent.ExecutionContext

class UnicoreApiItSpec extends scalatest.FlatSpec with scalatest.Matchers with scalatest.PrivateMethodTester {
  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  implicit val ec: ExecutionContext = akkaRuntime.executionContext
  implicit val mat = testutils.FakeAkkaRuntime.createMaterialier()

  val path = "systemtest.conf"
  val config = HpcExecutorConfig.fromTypesafeConfig(typesafe.config.ConfigFactory.parseResources(path))
  val apiUrl = "https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core"
  val httpClient = new http.Client(apiUrl)
  val credentials = Credentials(config.unicore.login.user, config.unicore.login.password)
  val unicoreApi = new UnicoreApi(httpClient = httpClient, credentials = credentials)

  "The UNICORE client" should "give the UNICORE version" in {
    val versionRegex = "^[0-9]*\\.[0-9]*\\.[0-9]*$"

    val method = unicoreApi.version()
    val result = testutils.Concurrent.await(method, maxWaitTime = 30)

    result.matches(versionRegex) should be(true)
  }

  it should "submit a job" in {
    val jobTags = List("test-job")
    val jobDescription = api.requests.JobDescription(
      executable = "echo",
      arguments = List("test"),
      project = config.unicore.accounting.project,
      tags = Some(jobTags)
    )
    val job = unicoreApi.submitJob(jobDescription)
    val method = job.flatMap(_.properties())
    val result = testutils.Concurrent.await(method, maxWaitTime = 30)
    println(result)

    result.tags should be(jobTags)
  }

  it should "submit a job with an inline import" in {
    val jobTags = List("test-job")
    val jobDescription = api.requests.JobDescription(
      executable = "echo",
      arguments = List("test"),
      project = config.unicore.accounting.project,
      tags = Some(jobTags),
      imports = Some(
        List(
          api.requests.UnicoreIO(
            from = "inline://test",
            to = "test.txt",
            data = Some("test123")
          )
        )
      )
    )
    val expected = ByteString("test")

    val job = unicoreApi.submitJob(jobDescription)
    val properties = job.flatMap(_.properties())
    val resultProperties = testutils.Concurrent.await(properties, maxWaitTime = 30)
    val stdoudFile = job.flatMap(_.getFile("stdout"))
    val stdout = stdoudFile.flatMap(_.download().flatMap(testutils.api.http.Streaming.materializeByteSource))
    // Set debug point in the following line
    // TODO: in `job.getFile`, the job should wait until it is finished
    // otherwise we get an error from `io.circe`.
    val resultStdout = testutils.Concurrent.await(stdout, maxWaitTime = 180)
    val uploadedFile = job.flatMap(_.getFile("stdout"))
    val uploadedFileDownloaded =
      uploadedFile.flatMap(_.download().flatMap(testutils.api.http.Streaming.materializeByteSource))
    // Set debug point in the following line
    // TODO: in `job.getFile`, the job should wait until it is finished
    // otherwise we get an error from `io.circe`.
    val result = testutils.Concurrent.await(uploadedFileDownloaded, maxWaitTime = 180)

    resultProperties.tags should be(jobTags)
    resultStdout should be(expected)
    result should be(expected)
  }
}
