package hpc.unicore.api.responses

import org.scalatest

import hpc.unicore.testutils.Json

class CoreSpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The Core JSON case class" should "decode a response when logged in" in {
    val response =
      """
        |{
        |  "server":{
        |    "jobSubmission":{
        |      "message":"test-message",
        |      "enabled":true
        |    },
        |    "credential":{
        |      "dn":"CN=fzj-njs.fz-juelich.de,OU=UNICORE Unicorex Juwels,OU=JSC,O=Forschungszentrum Juelich GmbH,L=Juelich,ST=Nordrhein-Westfalen,C=DE",
        |      "issuer":"CN=DFN-Verein Global Issuing CA,OU=DFN-PKI,O=Verein zur Foerderung eines Deutschen Forschungsnetzes e. V.,C=DE"
        |    },
        |    "externalConnections":{
        |      "UFTPD Server":"OK",
        |      "XUUDB attribute source":"OK",
        |      "Gateway":"OK",
        |      "Registry":"OK",
        |      "UFTPD judac05.fz-juelich.de:64333":"OK",
        |      "TSI 1":"OK"
        |    },
        |    "trustedCAs":[
        |      "CN=LIP Certification Authority,O=LIPCA,C=PT",
        |      "CN=Scientific Data Grid CA - G2,DC=SDG,DC=Grid,DC=CN"
        |    ],
        |    "version":"8.2.0",
        |    "trustedSAMLIssuers":[
        |      "CN=unity-jsc.fz-juelich.de,OU=JSC,O=Forschungszentrum Juelich GmbH,L=Juelich,ST=Nordrhein-Westfalen,C=DE"
        |    ]
        |  },
        |  "_links":{
        |    "storages":{
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storages"
        |    },
        |    "storagefactories":{
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storagefactories"
        |    }
        |  },
        |  "client":{
        |    "role":{
        |      "selected":"user",
        |      "availableRoles":[
        |        "user"
        |      ]
        |    },
        |    "queues":{
        |      "availableQueues":[
        |        "batch",
        |        "devel"
        |      ],
        |      "selected":"gpus"
        |    },
        |    "dn":"UID=test-user@email.domain",
        |    "xlogin":{
        |      "UID":"test-user",
        |      "availableGroups":[
        |        "test-group"
        |      ],
        |      "availableUIDs":[
        |        "test-user"
        |      ],
        |      "group":"test-group"
        |    }
        |  }
        |}
        |
        |""".stripMargin
    val result = Json.decodeJsonString[Core](response)

    result.server.jobSubmission.message should be("test-message")
    result.server.jobSubmission.enabled should be(true)
    result.server.version should be("8.2.0")
    result.client.role.selected should be("user")
    result.client.role.availableRoles should be(List("user"))
    result.client.queues.availableQueues should be(List("batch", "devel"))
    result.client.queues.selected should be("gpus")
    result.client.login.user.getOrElse(None) should be("test-user")
    result.client.login.availableGroups.getOrElse(None) should be(List("test-group"))
    result.client.login.availableUserIds.getOrElse(None) should be(List("test-user"))
    result.client.login.group.getOrElse(None) should be("test-group")
    result.loggedIn should be(true)
  }

  it should "decode a response when not logged in" in {
    val response =
      """
        |{
        |  "server":{
        |    "jobSubmission":{
        |      "message":"test-message",
        |      "enabled":true
        |    },
        |    "credential":{
        |      "dn":"CN=fzj-njs.fz-juelich.de,OU=UNICORE Unicorex Juwels,OU=JSC,O=Forschungszentrum Juelich GmbH,L=Juelich,ST=Nordrhein-Westfalen,C=DE",
        |      "issuer":"CN=DFN-Verein Global Issuing CA,OU=DFN-PKI,O=Verein zur Foerderung eines Deutschen Forschungsnetzes e. V.,C=DE"
        |    },
        |    "externalConnections":{
        |      "UFTPD Server":"OK",
        |      "XUUDB attribute source":"OK",
        |      "Gateway":"OK",
        |      "Registry":"OK",
        |      "UFTPD judac05.fz-juelich.de:64333":"OK",
        |      "TSI 1":"OK"
        |    },
        |    "trustedCAs":[
        |      "CN=LIP Certification Authority,O=LIPCA,C=PT",
        |      "CN=Scientific Data Grid CA - G2,DC=SDG,DC=Grid,DC=CN"
        |    ],
        |    "version":"8.2.0",
        |    "trustedSAMLIssuers":[
        |      "CN=unity-jsc.fz-juelich.de,OU=JSC,O=Forschungszentrum Juelich GmbH,L=Juelich,ST=Nordrhein-Westfalen,C=DE"
        |    ]
        |  },
        |  "_links":{
        |    "storages":{
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storages"
        |    },
        |    "storagefactories":{
        |      "href":"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/storagefactories"
        |    }
        |  },
        |  "client":{
        |    "role":{
        |      "selected":"user",
        |      "availableRoles":[
        |        "user"
        |      ]
        |    },
        |    "queues":{
        |      "availableQueues":[
        |        "batch",
        |        "devel"
        |      ],
        |      "selected":"gpus"
        |    },
        |    "dn":"UID=test-user@email.domain",
        |    "xlogin":{
        |    
        |    }
        |  }
        |}
        |
        |""".stripMargin
    val result = Json.decodeJsonString[Core](response)

    result.server.jobSubmission.message should be("test-message")
    result.server.jobSubmission.enabled should be(true)
    result.server.version should be("8.2.0")
    result.client.role.selected should be("user")
    result.client.role.availableRoles should be(List("user"))
    result.client.queues.availableQueues should be(List("batch", "devel"))
    result.client.queues.selected should be("gpus")
    result.client.login.user should be(None)
    result.client.login.availableGroups should be(None)
    result.client.login.availableUserIds should be(None)
    result.client.login.group should be(None)
    result.loggedIn should be(false)
  }
}
