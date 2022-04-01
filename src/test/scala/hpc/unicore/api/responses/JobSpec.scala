package hpc.unicore.api.responses

import hpc.unicore.testutils
import org.scalatest

class JobSpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The Job JSON case class" should "decode a response" in {
    val response =
      """
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
        |  "status":"SUCCESSFUL"
        |}
        |""".stripMargin
    val result = testutils.Json.decodeJsonString[Job](response)

    result.links.base.url should be("test-url-self")
    result.owner should be("UID=test@mail.de")
    result.logs should be(List("test-log-message-1", "test-log-message-2"))
    result.resourceStatusMessage should be("test-resource-status-message")
    result.siteName should be("TEST-SITE")
    result.consumedTime.postCommand should be("1")
    result.consumedTime.total should be("2")
    result.consumedTime.queued should be("3")
    result.consumedTime.preCommand should be("4")
    result.consumedTime.main should be("5")
    result.submissionTime should be("test-submission-time")
    result.statusMessage should be("test-status-message")
    result.tags should be(List("test-tag-1", "test-tag-2"))
    result.currentTime should be("test-current-time")
    result.resourceStatus should be("READY")
    result.batchSystemID should be("test-id")
    result.terminationTime should be("test-termination-time")
    result.name should be("Test-job")
    result.queue should be("devel")
    result.status should be("SUCCESSFUL")
  }
}
