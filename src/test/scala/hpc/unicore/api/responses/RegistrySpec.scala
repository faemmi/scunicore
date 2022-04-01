package hpc.unicore.api.responses

import org.scalatest

import hpc.unicore.testutils.Json

class RegistrySpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The Registry JSON case class" should "decode a response" in {
    val response =
      """
        |{
        |  "owner":"testowner",
        |  "currentTime":"testcurrenttime",
        |  "resourceStatus":"READY",
        |  "entries":[
        |    {
        |      "ServerIdentity":"identity1",
        |      "InterfaceNamespace":"interface-url-1",
        |      "href":"api-url-1",
        |      "type":"apitype1"
        |    },
        |    {
        |      "ServerIdentity":"identity2",
        |      "InterfaceNamespace":"interface-url-2",
        |      "href":"api-url-2",
        |      "type":"apitype2"
        |    }
        |  ],
        |  "_links":{
        |    "self":{
        |      "href":"https://fzj-unic.fz-juelich.de:9112/FZJ/rest/registries/default_registry"
        |    }
        |  },
        |  "resourceStatusMessage":"statusmessage",
        |  "siteName":"FZJ",
        |  "acl":[
        |
        |  ],
        |  "tags":[
        |    "tag"
        |  ]
        |}
        |""".stripMargin
    val result = Json.decodeJsonString[Registry](response)

    result.owner should be("testowner")
    result.currentTime should be("testcurrenttime")
    result.resourceStatus should be("READY")
    result.entries(0).ServerIdentity should be("identity1")
    result.entries(0).InterfaceNamespace should be("interface-url-1")
    result.entries(0).href should be("api-url-1")
    result.entries(0).apiType should be("apitype1")
    result.entries(1).ServerIdentity should be("identity2")
    result.entries(1).InterfaceNamespace should be("interface-url-2")
    result.entries(1).href should be("api-url-2")
    result.entries(1).apiType should be("apitype2")
    result.resourceStatusMessage should be("statusmessage")
    result.siteName should be("FZJ")
    result.tags should be(List("tag"))
  }
}
