package ai.mantik.executor.hpc.api.unicore.api

import ai.mantik.componently.{AkkaRuntime, ComponentBase}
import ai.mantik.executor.hpc.api
import ai.mantik.executor.hpc.api.http
import akka.actor
import akka.http.scaladsl.model.HttpRequest

import scala.concurrent.Future

/** A job that was created by submitting a [[api.unicore.api.requests.JobDescription]] to the UNICORE API.
  *
  * @constructor Create a job that has been deployed via the UNICORE API.
  * @param id The ID of the job assigned by UNICORE.
  * @param httpClient Must contain the UNICORE API URL of the computation site.
  */
class Job(
    val id: String,
    httpClient: http.Client
)(implicit akkaRuntime: AkkaRuntime)
    extends ComponentBase
    with api.Job
    with Resource[responses.Job] {
  protected val client: http.Client = httpClient

  override def toString: String =
    s"${getClass.getName}(id = $id, url = ${client.baseUrl}/${Endpoints.Jobs.baseForSubmittedJob(id)})"

  def directory(): Future[WorkingDirectory] = links().map(links => {
    WorkingDirectory.fromUrl(links.workingDirectory.url, httpClient = client)
  })

  def links(): Future[responses.Job.Links] = properties().map(_.links)

  override def properties(): Future[responses.Job] = {
    logger.debug(s"Getting properties for $this")
    val endpoint = Endpoints.Jobs.properties(id)
    val result = client.sendRequestAndDecodeResponseToString(endpoint)
    Decoder.decodeJsonString[responses.Job](result)
  }

  /** Returns the job status. */
  override def status(): Future[api.Job.Status.Value] =
    properties().map(p => api.Job.Status.withName(p.status))

  /** Returns if the job is running. */
  override def isRunning(): Future[Boolean] = status().map({
    case api.Job.Status.Failed | api.Job.Status.Successful => false
    case _                                                 => true
  })

  /** Returns if the job has failed. */
  override def isFailure(): Future[Boolean] = status().map(_ == api.Job.Status.Failed)

  /** Returns if the job was successful. */
  override def isSuccess(): Future[Boolean] = status().map(_ == api.Job.Status.Successful)

  /** Start the job.
    *
    * If imports were given (i.e. files uploaded before execution),
    * the job will be in "STAGINGIN" state, and "READY" if all files
    * have been uploaded. Then, it requires to be manually started.
    */
  override def start(): Future[Unit] = {
    logger.debug(s"Starting $this")
    val request = client.createPostRequest(Endpoints.Jobs.start(id))
    tryRequest(request, messageIfFailed = "Job could not be started")
  }

  /** Delete the job.
    *
    * Immediately stops the job and deletes its working directory.
    */
  override def delete(): Future[Unit] = {
    logger.debug(s"Deleting $this")
    val request = client.createDeleteRequest(Endpoints.Jobs.delete(id))
    tryRequest(request, messageIfFailed = "Job could not be deleted")
  }

  /** Abort the job. */
  override def abort(): Future[Unit] = {
    logger.debug(s"Aborting $this")
    val request = client.createPostRequest(Endpoints.Jobs.abort(id))
    tryRequest(request, messageIfFailed = "Job could not be aborted")
  }

  private def tryRequest(request: HttpRequest, messageIfFailed: String): Future[Unit] = {
    val response = client.sendRequest(request)
    response.map(resp => if (resp.status.isFailure) throw new RuntimeException(messageIfFailed))
  }

  def getFile(file: String): Future[File] = {
    logger.debug(s"Getting file $file from working directory of job $this")
    directory().flatMap(_.getFile(file))
    // Cannot access any file while the job is still running.
    // TODO: How to wait here until job is done?
    //isRunning().flatMap(jobIsRunning => {
    //  while (jobIsRunning) {
    //    logger.debug(s"Cannot download file $file yet, $this is still running")
    //    val props = properties().map(p => p)
    //    logger.debug(s"Job properties: ${props}")
    //    // Wait for 1 second to check job status again.
    //    // TODO: How to achieve a frequent, delayed check without blocking the Thread?
    //    Thread.sleep(1000)
    //    getFile(file)
    //  }
    //  logger.debug(s"Getting file $file from working directory of job $this")
    //  directory().flatMap(_.getFile(file))
    //})
  }

}

/** Wraps a job to be an [[akka.actor.Cancellable]]. */
class CancellableJob(
    job: Job
)(implicit akkaRuntime: AkkaRuntime)
    extends ComponentBase
    with actor.Cancellable {
  private var cancelled = false
  override def cancel(): Boolean = {
    // TODO: How to await if cancellation was successful?
    job.abort()
    //  .map( response =>
    //  true
    //) recover {
    //  case _ => false
    //}
    cancelled = true
    true
  }

  override def isCancelled: Boolean = cancelled
}
