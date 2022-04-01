package ai.mantik.executor.hpc.api.unicore.api.responses

import org.scalatest

import ai.mantik.executor.hpc.testutils.Json

class JobsSpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The Jobs JSON case class" should "decode a response" in {
    val response =
      """
        |{
        |  "_links":{
        |    "self":{
        |      "href":"request-url"
        |    }
        |  },
        |  "jobs":[
        |    "job-url-1",
        |    "job-url-2"
        |  ]
        |}
        |""".stripMargin
    val result = Json.decodeJsonString[Jobs](response)

    result.urls should be(List("job-url-1", "job-url-2"))
  }
}
