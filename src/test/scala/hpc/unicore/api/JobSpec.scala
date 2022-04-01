package hpc.unicore.api

import hpc.unicore.testutils
import hpc.unicore.api
import akka.util.ByteString
import org.scalatest

class JobSpec extends scalatest.FlatSpec with scalatest.Matchers {
  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  val client = new testutils.api.http.FakeClient()
  val job = new Job(id = "test-id", httpClient = client)

  "A job" should "deliver its properties" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "READY"))
    val method = job.properties()
    val result = testutils.Concurrent.await(method)

    result.status should be("READY")
  }

  it should "report its status" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "UNKNOWN"))
    val method = job.status()
    val result = testutils.Concurrent.await(method)

    result should be(api.Job.Status.Unknown)
  }

  it should "report that it's not running when failure" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "FAILED"))
    val method = job.isRunning()
    val result = testutils.Concurrent.await(method)

    result should be(false)
  }
  it should "report that it's not running when successful" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "SUCCESSFUL"))
    val method = job.isRunning()
    val result = testutils.Concurrent.await(method)

    result should be(false)

  }
  it should "report that it's running when queued" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "QUEUED"))
    val method = job.isRunning()
    val result = testutils.Concurrent.await(method)

    result should be(true)
  }

  it should "report that it has failed" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "FAILED"))
    val method = job.isFailure()
    val result = testutils.Concurrent.await(method)

    result should be(true)

  }
  it should "report that it has not failed" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "SUCCESSFUL"))
    val method = job.isFailure()
    val result = testutils.Concurrent.await(method)

    result should be(false)
  }

  it should "report that it was not successful" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "FAILED"))
    val method = job.isSuccess()
    val result = testutils.Concurrent.await(method)

    result should be(false)

  }
  it should "report that it was successful" in {
    client.respondsWith = ByteString(JobSpec.createResponse(status = "SUCCESSFUL"))
    val method = job.isSuccess()
    val result = testutils.Concurrent.await(method)

    result should be(true)
  }
}

object JobSpec {
  def createResponse(status: String): String = {
    s"""
       |{
       |  "owner":"UID=test@mail.de",
       |  "submissionPreferences":{
       |
       |  },
       |  "log":[
       |    "test-log-message-1",
       |    "test-log-message-2"
       |  ],
       |  "_links":{
       |    "self":{
       |      "href":"test-url-self"
       |    },
       |    "action:start":{
       |      "description":"Start",
       |      "href":"test-url-start"
       |    },
       |    "action:restart":{
       |      "description":"Restart",
       |      "href":"test-url-restart"
       |    },
       |    "action:abort":{
       |      "description":"Abort",
       |      "href":"test-url-abort"
       |    },
       |    "workingDirectory":{
       |      "description":"Working directory",
       |      "href":"test-url-working-directory"
       |    },
       |    "details":{
       |      "description":"Details",
       |      "href":"test-url-details"
       |    }
       |  },
       |  "resourceStatusMessage":"test-resource-status-message",
       |  "siteName":"TEST-SITE",
       |  "consumedTime":{
       |    "postCommand":"1",
       |    "total":"2",
       |    "stage-out":"0",
       |    "queued":"3",
       |    "preCommand":"4",
       |    "main":"5",
       |    "stage-in":"0"
       |  },
       |  "acl":[
       |
       |  ],
       |  "submissionTime":"test-submission-time",
       |  "statusMessage":"test-status-message",
       |  "tags":[
       |    "test-tag-1",
       |    "test-tag-2"
       |  ],
       |  "currentTime":"test-current-time",
       |  "resourceStatus":"READY",
       |  "batchSystemID":"test-id",
       |  "terminationTime":"test-termination-time",
       |  "name":"Test-job",
       |  "queue":"devel",
       |  "status":"$status"
       |}
       |""".stripMargin

  }
}
