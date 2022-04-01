package hpc.unicore.api.responses

import hpc.unicore.testutils
import org.scalatest

class PathSpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The Path JSON case class" should "decode a response of a path" in {
    val response =
      """
        |{
        |  "owner":"UID=test-user@email.domain",
        |  "size":4096,
        |  "_links":{
        |    "next":{
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storages/fe0d15f0-9644-4f55-af74-325449128669-uspace/files?offset=4&num=4"
        |    },
        |    "self":{
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storages/fe0d15f0-9644-4f55-af74-325449128669-uspace/files?offset=0&num=4"
        |    },
        |    "parentStorage":{
        |      "description":"Parent Storage",
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storages/fe0d15f0-9644-4f55-af74-325449128669-uspace"
        |    }
        |  },
        |  "permissions":"rwx------",
        |  "lastAccessed":"2022-03-22T11:43:48+0100",
        |  "isDirectory":true,
        |  "content":{
        |    "/UNICORE_SCRIPT_EXIT_CODE":{
        |      "owner":"test-owner",
        |      "size":2,
        |      "permissions":"rw-------",
        |      "lastAccessed":"2022-03-22T11:43:48+0100",
        |      "isDirectory":false,
        |      "group":"test-group"
        |    },
        |    "/UNICORE_SCRIPT_PID":{
        |      "owner":"test-owner",
        |      "size":6,
        |      "permissions":"rw-------",
        |      "lastAccessed":"2022-03-22T11:43:48+0100",
        |      "isDirectory":false,
        |      "group":"test-group"
        |    },
        |    "/stderr":{
        |      "owner":"test-owner",
        |      "size":0,
        |      "permissions":"rw-------",
        |      "lastAccessed":"2022-03-22T11:43:48+0100",
        |      "isDirectory":false,
        |      "group":"test-group"
        |    },
        |    "/stdout":{
        |      "owner":"test-owner",
        |      "size":3,
        |      "permissions":"rw-------",
        |      "lastAccessed":"2022-03-22T11:43:48+0100",
        |      "isDirectory":false,
        |      "group":"test-group"
        |    }
        |  },
        |  "group":"test-group"
        |}
        |""".stripMargin
    val expectedStdoutFile = Path.Element(
      owner = "test-owner",
      group = "test-group",
      isDirectory = false,
      size = 3,
      permissions = "rw-------"
    )

    val result = testutils.Json.decodeJsonString[Path](response)

    result.isDirectory should be(true)
    result.owner should be("UID=test-user@email.domain")
    result.group should be("test-group")
    result.content match {
      case Some(content) => content.get("/stdout") should be(Some(expectedStdoutFile))
      case None          => fail("No content read")
    }
  }

  it should "decode a response of a file" in {
    val response =
      """
        |{
        |  "owner":"test-owner",
        |  "size":3,
        |  "_links":{
        |    "parentStorage":{
        |      "description":"Parent Storage",
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storages/fe0d15f0-9644-4f55-af74-325449128669-uspace"
        |    }
        |  },
        |  "permissions":"rw-------",
        |  "lastAccessed":"2022-03-22T11:43:48+0100",
        |  "isDirectory":false,
        |  "group":"test-group"
        |}
        |""".stripMargin
    val result = testutils.Json.decodeJsonString[Path](response)

    result.isDirectory should be(false)
    result.owner should be("test-owner")
    result.group should be("test-group")
  }
}
