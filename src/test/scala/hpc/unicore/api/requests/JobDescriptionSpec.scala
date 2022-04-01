package hpc.unicore.api.requests

import scala.language.postfixOps
import hpc.unicore.testutils

import akka.http.scaladsl.model.ContentTypes
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
  implicit val actor = akka.actor.ActorSystem()
  implicit val materializer = akka.stream.Materializer(actor)

  "The job class" should "generate an httpEntity correctly" in {
    val expected =
      """{"Executable":"/bin/ls","Arguments":["."],"Project":"test project","Job type":"normal","haveClientStageIn":true,"Resources":{"Queue":"batch","Nodes":1},"Imports":[{"From":"here","To":"there","FailOnError":true}]}"""
    val ent = job.toHttpEntity
    ent.contentType should be(ContentTypes.`application/json`)
    val answer = ent.getDataBytes().map(s => s.utf8String).runWith(Sink.head[String], materializer)
    val answerMaterialized = testutils.Concurrent.await(answer)

    answerMaterialized should be(expected)
  }

}
