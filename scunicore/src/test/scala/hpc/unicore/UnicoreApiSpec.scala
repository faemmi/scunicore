package ai.mantik.executor.hpc.api.unicore

import akka.http.scaladsl.model.{Uri, headers}
import akka.util.ByteString
import org.scalatest

import ai.mantik.executor.hpc.api.unicore.api.requests
import ai.mantik.executor.hpc.testutils

class UnicoreApiSpec extends scalatest.FlatSpec with scalatest.Matchers with scalatest.PrivateMethodTester {

  implicit val akkaRuntime = testutils.FakeAkkaRuntime.create()
  val httpClient = new testutils.api.http.FakeClient()
  val client = new UnicoreApi(httpClient = httpClient, credentials = Credentials("", ""))
  val getJobIdFromApiUrl = PrivateMethod[String](Symbol("getJobIdFromApiUrl"))

  val jobId = "cb20785f-e32a-43b8-9aab-954f09643087"
  val jobUrl = s"https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/jobs/$jobId"

  "The UNICORE client" should "give the UNICORE version" in {
    httpClient.respondsWith = ByteString(ClientSuite.createResponse(version = "1.2.3"))
    val method = client.version()
    val result = testutils.Concurrent.await(method)

    result should be("1.2.3")
  }

  it should "submit a job" in {
    val resources = requests.HpcResources()
    val job = requests.JobDescription(
      executable = "test",
      arguments = List("job"),
      resources = Some(resources),
      project = "test project"
    )
    httpClient.responseHeaders = Seq(headers.Location(Uri(jobUrl)))
    val method = client.submitJob(job)
    val result = testutils.Concurrent.await(method)

    result.id should be(jobId)
  }

  it should "list all jobs that have been submitted" in {
    httpClient.respondsWith = ByteString(s"""{"jobs":["$jobUrl","$jobUrl"]}""")
    val method = client.listJobs()
    val result = testutils.Concurrent.await(method)

    result.length should be(2)
  }

  it should "get the correct job ID from an URL" in {
    val invalidUrl = "https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/jobs/invalid/id"
    val result = UnicoreApi invokePrivate getJobIdFromApiUrl(jobUrl)

    result should be(jobId)

    a[RuntimeException] should be thrownBy (UnicoreApi invokePrivate getJobIdFromApiUrl(invalidUrl))
  }
}

object ClientSuite {
  def createResponse(version: String): String = {
    s"""
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
       |    "version":"$version",
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

  }
}
