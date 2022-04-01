package ai.mantik.executor.hpc.api.unicore.api.requests

import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.util.ByteString
import io.circe
import io.circe.generic
import ai.mantik.executor.hpc.api
import io.circe.syntax._

import scala.collection.mutable.ListBuffer

/** Description of a job to be executed.
  *
  * For details see ´https://sourceforge.net/p/unicore/wiki/Job_Description/´.
  *
  * @param executable Executable to use (as via CLI, e.g. `/bin/ls`).
  * @param arguments Arguments to be passed to the executable.
  * @param project Project to use for accounting.
  * @param resources HPC resource definition.
  * @param environment Environment variables to set in the execution environment.
  * @param imports Data to import into the job directory before executing the job.
  * @param exports Data to export from the job directory after job execution.
  * @param name Name of the job.
  * @param tags Tags that can be used to filter the job when listing.
  * @param jobType Types: normal: batch, interactive: login node.
  */
case class JobDescription(
    @generic.extras.JsonKey("Executable") executable: String,
    @generic.extras.JsonKey("Arguments") arguments: List[String],
    @generic.extras.JsonKey("Project") project: String,
    @generic.extras.JsonKey("Resources") resources: Option[HpcResources] = None,
    @generic.extras.JsonKey("Environment") environment: Option[Map[String, String]] = None,
    @generic.extras.JsonKey("Imports") imports: Option[List[UnicoreIO]] = None,
    @generic.extras.JsonKey("Exports") exports: Option[List[UnicoreIO]] = None,
    @generic.extras.JsonKey("Name") name: Option[String] = None,
    @generic.extras.JsonKey("Tags") tags: Option[List[String]] = None,
    @generic.extras.JsonKey("Job type") jobType: String = "interactive",
    @generic.extras.JsonKey("User email") email: Option[String] = None
) extends api.JobDefinition {
  // If imports are given, job waits until they are uploaded and then
  // has to be triggered manually.
  val haveClientStageIn: Boolean = setTrueIfImportsGiven(imports)

  override def toHttpEntity: HttpEntity = {
    implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    HttpEntity(
      contentType = ContentTypes.`application/json`,
      data = ByteString(JobDescription.encoder(this).noSpaces)
    )
  }

  private def setTrueIfImportsGiven(imp: Option[List[UnicoreIO]]): Boolean = imp match {
    case None => false
    case Some(i) =>
      i match {
        case Nil => false
        case _   => true
      }
  }
}

object JobDescription {
  implicit val encoder: circe.Encoder[JobDescription] = (d: JobDescription) => asJson(d)

  def asJson(job: JobDescription): circe.Json = {
    val fields = ListBuffer[(String, circe.Json)]()
    fields.appendAll(
      List(
        ("Executable", job.executable.asJson),
        ("Arguments", job.arguments.asJson),
        ("Project", job.project.asJson),
        ("Job type", job.jobType.asJson),
        ("haveClientStageIn", job.haveClientStageIn.asJson)
      )
    )

    job.resources match {
      case Some(resources) => fields.append(("Resources", HpcResources.asJson(resources)))
      case None            =>
    }

    job.environment match {
      case Some(environment) => fields.append(("Environment", environment.asJson))
      case None              =>
    }

    job.imports match {
      case Some(imports) => fields.append(("Imports", imports.map(UnicoreIO.asJson).asJson))
      case None          =>
    }
    job.exports match {
      case Some(exports) => fields.append(("Exports", exports.map(UnicoreIO.asJson).asJson))
      case None          =>
    }
    job.name match {
      case Some(name) => fields.append(("Imports", name.asJson))
      case None       =>
    }
    job.tags match {
      case Some(tags) => fields.append(("Tags", tags.asJson))
      case None       =>
    }
    job.email match {
      case Some(email) => fields.append(("User email", email.asJson))
      case None        =>
    }

    circe.Json.fromFields(fields)
  }

}

/** A UNICORE import/export.
  *
  * @param from File to copy into job directory.
  *             `file://` - copy file(s) from local machine
  *             `link://` - symlink file/directory from local machine
  *             `unicore://` - resolve location from some UNICORE server
  *             `ftp://` - resolve location from some FTP server
  *             `inline://` - place data directly in the JSON request (see [[data]])
  * @param to File name (and path) where to put the file in the job directory.
  * @param data Data to put into the file if `inline://` is used as prefix in [[from]].
  * @param failOnError Whether to fail on error.
  * @param preferredProtocols Preferred protocol.
  * @param credentials Credentials for a remote file service.
  */
@generic.JsonCodec
case class UnicoreIO(
    @generic.extras.JsonKey("From") from: String,
    @generic.extras.JsonKey("To") to: String,
    @generic.extras.JsonKey("Data") data: Option[String] = None,
    @generic.extras.JsonKey("FailOnError") failOnError: Boolean = true,
    @generic.extras.JsonKey("PreferredProtocols") preferredProtocols: Option[List[String]] = None,
    @generic.extras.JsonKey("Credentials") credentials: Option[Credentials] = None
)

object UnicoreIO {
  implicit val encoder: circe.Encoder[UnicoreIO] = (i: UnicoreIO) => asJson(i)

  def asJson(io: UnicoreIO): circe.Json = {
    val fields = ListBuffer[(String, circe.Json)]()

    fields.appendAll(
      List(
        ("From", io.from.asJson),
        ("To", io.to.asJson),
        ("FailOnError", io.failOnError.asJson)
      )
    )

    io.data match {
      case Some(data) => fields.append(("Data", data.asJson))
      case None       =>
    }
    io.preferredProtocols match {
      case Some(preferredProtocols) => fields.append(("PreferredProtocols", preferredProtocols.asJson))
      case None                     =>
    }
    io.credentials match {
      case Some(credentials) => fields.append(("Credentials", credentials.asJson))
      case None              =>
    }

    circe.Json.fromFields(fields)
  }
}

/** Credentials for a remote file service */
@generic.JsonCodec
case class Credentials(username: String, password: String)

/** Options for the HPC resources.
  *
  * @param queue Queue to place the job in. Default is `batch`, but we use devel queue here.
  * @param nodes Number of computing nodes to use for the job.
  * @param cpus Total number of CPUs.
  * @param cpusPerNode Number of CPUs per node.
  * @param memory Memory per node.
  * @param runtime Job runtime (wall time).
  *                Default is seconds. Other options are `min`, `h`, `d` that have to be passed explicitly.
  * @param reservation Batch system reservation ID.
  * @param nodeConstraints Batch system node constraints.
  * @param qos Batch system QoS.
  */
@generic.JsonCodec
case class HpcResources(
    @generic.extras.JsonKey("Queue") queue: String = "devel",
    @generic.extras.JsonKey("Nodes") nodes: Int = 1,
    @generic.extras.JsonKey("CPUs") cpus: Option[Int] = None,
    @generic.extras.JsonKey("CPUsPerNode") cpusPerNode: Option[Int] = None,
    @generic.extras.JsonKey("Memory") memory: Option[String] = None,
    @generic.extras.JsonKey("Runtime") runtime: Option[String] = None,
    @generic.extras.JsonKey("Reservation") reservation: Option[String] = None,
    @generic.extras.JsonKey("NodeConstraints") nodeConstraints: Option[String] = None,
    @generic.extras.JsonKey("QoS") qos: Option[String] = None
)

object HpcResources {
  implicit val encoder: circe.Encoder[HpcResources] = (r: HpcResources) => asJson(r)

  def asJson(resources: HpcResources): circe.Json = {
    val fields = ListBuffer[(String, circe.Json)]()

    fields.appendAll(
      List(
        ("Queue", resources.queue.asJson),
        ("Nodes", resources.nodes.asJson)
      )
    )

    resources.cpus match {
      case Some(cpus) => fields.append(("CPUs", cpus.asJson))
      case None       =>
    }
    resources.cpusPerNode match {
      case Some(cpusPerNode) => fields.append(("CPUsPerNode", cpusPerNode.asJson))
      case None              =>
    }
    resources.memory match {
      case Some(memory) => fields.append(("Memory", memory.asJson))
      case None         =>
    }
    resources.runtime match {
      case Some(runtime) => fields.append(("Runtime", runtime.asJson))
      case None          =>
    }
    resources.reservation match {
      case Some(reservation) => fields.append(("Reservation", reservation.asJson))
      case None              =>
    }
    resources.nodeConstraints match {
      case Some(nodeConstraints) => fields.append(("NodeConstraints", nodeConstraints.asJson))
      case None                  =>
    }
    resources.qos match {
      case Some(qos) => fields.append(("QoS", qos.asJson))
      case None      =>
    }

    circe.Json.fromFields(fields)
  }
}
