package ai.mantik.executor.hpc.api.unicore.api.requests

import scala.language.postfixOps
import ai.mantik.executor.hpc.testutils

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.scalatest

class JobDescriptionSpec extends scalatest.FlatSpec with scalatest.Matchers {
  val resources = HpcResources()
  val job = JobDescription(
    executable = "/bin/ls",
    arguments = List("."),
    resources = Some(resources),
    project = "test project",
    imports = Some(
      List(
        UnicoreIO(from = "here", to = "there")
      )
    )
  )
  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

  "The job class" should "generate an httpEntity correctly" in {
    val expected =
      """{"Executable":"/bin/ls","Arguments":["."],"Project":"test project","Job type":"interactive","haveClientStageIn":true,"Resources":{"Queue":"devel","Nodes":1},"Imports":[{"From":"here","To":"there","FailOnError":true}]}"""
    val ent = job.toHttpEntity
    ent.contentType should be(ContentTypes.`application/json`)
    val answer = ent.getDataBytes().map(s => s.utf8String).runWith(Sink.head[String], actorMaterializer)
    val answerMaterialized = testutils.Concurrent.await(answer)

    answerMaterialized should be(expected)
  }

}
