package hpc.unicore.api.responses

import hpc.unicore.testutils
import org.scalatest

class WorkingDirectorySpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The WorkingDirectory JSON case class" should "decode a response" in {
    val response =
      """
        |{
        |  "owner":"UID=fabian.emmerich@4-cast.de",
        |  "umask":"77",
        |  "mountPoint":"/p/scratch/deepacf/unicore-jobs//dd8cab9f-d34f-4501-9501-4862c46a678a/",
        |  "freeSpace":169225654108160,
        |  "_links":{
        |    "action:rename":{
        |      "description":"Rename file 'from' to file 'to'.",
        |      "href":"test-url-rename"
        |    },
        |    "self":{
        |      "href":"test-url-self"
        |    },
        |    "files":{
        |      "description":"Files",
        |      "href":"test-url-files"
        |    },
        |    "action:copy":{
        |      "description":"Copy file 'from' to file 'to'.",
        |      "href":"test-url-copy"
        |    }
        |  },
        |  "resourceStatusMessage":"N/A",
        |  "siteName":"JUWELS",
        |  "description":"Job's workspace",
        |  "acl":[
        |
        |  ],
        |  "usableSpace":-1,
        |  "tags":[
        |
        |  ],
        |  "currentTime":"2022-03-22T10:54:36+0100",
        |  "resourceStatus":"READY",
        |  "metadataSupported":false,
        |  "filesystemDescription":"UNICORE TSI at juwels01.fz-juelich.de:26303, juwels02.fz-juelich.de:26303, juwels03.fz-juelich.de:26303, juwels04.fz-juelich.de:26303, juwels05.fz-juelich.de:26303, juwels06.fz-juelich.de:26303, juwels07.fz-juelich.de:26303, juwels08.fz-juelich.de:26303, juwels09.fz-juelich.de:26303, juwels10.fz-juelich.de:26303, juwelsvis00.fz-juelich.de:26303, juwelsvis02.fz-juelich.de:26303, juwelsvis03.fz-juelich.de:26303, juwels21.fz-juelich.de:26303, juwels22.fz-juelich.de:26303, juwels23.fz-juelich.de:26303, juwels24.fz-juelich.de:26303",
        |  "protocols":[
        |    "UFTP",
        |    "BFT"
        |  ]
        |}
        |""".stripMargin
    val result = testutils.Json.decodeJsonString[WorkingDirectory](response)

    result.links.base.url should be("test-url-self")
    result.links.files.url should be("test-url-files")
    result.links.copy.url should be("test-url-copy")
    result.links.rename.url should be("test-url-rename")
    result.status should be("READY")
  }
}
