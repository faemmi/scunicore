package hpc.unicore

import scala.annotation.tailrec
import scala.concurrent.{Future, ExecutionContext}
import akka.http.scaladsl.model.{HttpHeader, HttpResponse, headers}

/** Connection to a given cluster's UNICORE API.
  *
  * @constructor Create a connection to the given UNICORE API with user authentication.
  * @param httpClient Must contain the UNICORE API URL of the computation site.
  * @param credentials User credentials to use for interaction with the API.
  */
class Api(
    httpClient: http.Client,
    credentials: Credentials
)(implicit protected val ec: ExecutionContext)
    extends Component
    with api.Resource[api.responses.Core] {
  protected val client: http.Client = httpClient
    .withAuthenticationHeader(credentials.user, credentials.password)
    .withJsonResponseHeader()

  override def toString: String = s"${getClass.getName}(url = ${client.baseUrl})"

  def properties(): Future[api.responses.Core] = {
    logger.debug(s"Getting properties for $this")
    val response = client.sendRequestAndDecodeResponseToString()
    api.Decoder.decodeJsonString[api.responses.Core](response)
  }

  /** Returns the UNICORE version of the server.
    *
    * Sends a `GET` request to the core API URL of the server.
    */
  def version(): Future[String] = properties().map(_.server.version)

  /** Submit a job to the UNICORE API, i.e. place it in the queue.
    *
    * @param job Description of the job that is to be deployed.
    *
    * @return The [[api.Job]] that was deployed.
    */
  def submitJob(job: api.requests.JobDescription): Future[api.Job] = {
    logger.debug(s"Submitting $job")
    val response = sendJobDescription(job)
    createJobInstance(response = response)
  }

  /** List all jobs deployed via UNICORE. */
  def listJobs(tags: List[String] = Nil): Future[List[api.Job]] = {
    logger.debug(s"Listing all jobs in $this")
    val response = getApiResponse(api.Endpoints.Jobs.list(tags = tags))
    createListOfJobsFromUrls(response)
  }

  private def sendJobDescription(job: api.requests.JobDescription): Future[HttpResponse] = {
    val request = client.createPostRequest(url = api.Endpoints.Jobs.submit, entity = job.toRequestEntity)
    client.sendRequest(request)
  }

  private def createJobInstance(response: Future[HttpResponse]): Future[api.Job] =
    response.map(r =>
      if (r.status.isSuccess) {
        val jobApiUrl = Api.getJobApiUrlFromHeaders(r.headers)
        val jobId = Api.getJobIdFromApiUrl(jobApiUrl)
        new api.Job(id = jobId, httpClient = client)
      } else {
        throw new RuntimeException(s"Job submission failed: ${r.status.reason}")
      }
    )

  private def getApiResponse(apiEndpoint: String): Future[api.responses.Jobs] = {
    val response = client.sendRequestAndDecodeResponseToString(apiEndpoint)
    api.Decoder.decodeJsonString[api.responses.Jobs](response)
  }

  private def createListOfJobsFromUrls(response: Future[api.responses.Jobs]): Future[List[api.Job]] = {
    response.map(r => r.urls.map(url => new api.Job(id = Api.getJobIdFromApiUrl(url), httpClient = client)))
  }
}

object Api {
  // Typically, a job API URL looks like
  // `https://zam2125.zam.kfa-juelich.de:9112/JUWELS/rest/core/jobs/cb20785f-e32a-43b8-9aab-954f09643087`
  private val jobIdUrlPattern = ".*//.*/jobs/([a-z0-9-]*)$".r

  /** Get the job API URL from the Location header of the response. */
  @tailrec
  private def getJobApiUrlFromHeaders(list: Seq[HttpHeader]): String = {
    if (list.isEmpty) throw new RuntimeException(s"Response headers $list do not contain a location")
    list.head match {
      case headers.Location(uri) => uri.toString()
      case _                     => getJobApiUrlFromHeaders(list.tail)
    }
  }

  private def getJobIdFromApiUrl(url: String): String = url match {
    case jobIdUrlPattern(jobId) => jobId
    case _                      => throw new RuntimeException(s"No valid job ID found in $url")
  }
}

/** JUDOOR user credentials for the login node. */
case class Credentials(user: String, password: String)
