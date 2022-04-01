package ai.mantik.executor.hpc.api.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import ai.mantik.executor.hpc.api.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting job properties. */
@generic.extras.ConfiguredJsonCodec
case class Job(
    @generic.extras.JsonKey("_links") links: Job.Links,
    owner: String,
    @generic.extras.JsonKey("log") logs: List[String],
    resourceStatusMessage: String,
    siteName: String,
    consumedTime: Job.ConsumedTime,
    submissionTime: String,
    statusMessage: String,
    tags: List[String],
    currentTime: String,
    resourceStatus: String,
    batchSystemID: String,
    terminationTime: String,
    name: String,
    queue: String,
    status: String
)

object Job {

  /** URLS to API endpoints that allow to interact with the job via `POST` requests.
    *
    * @param base URL to the job itself.
    * @param start URL to start the job.
    * @param restart URL to restart the job.
    * @param abort URL to abort the job.
    * @param workingDirectory Working directory of the job.
    * @param details Batch system level information.
    */
  @generic.extras.ConfiguredJsonCodec
  case class Links(
      @generic.extras.JsonKey("self") base: Link,
      @generic.extras.JsonKey("action:start") start: Link,
      @generic.extras.JsonKey("action:restart") restart: Link,
      @generic.extras.JsonKey("action:abort") abort: Link,
      @generic.extras.JsonKey("workingDirectory") workingDirectory: Link,
      details: Link
  )

  @generic.extras.ConfiguredJsonCodec
  case class ConsumedTime(
      postCommand: String,
      total: String,
      @generic.extras.JsonKey("stage-out") stageOut: String,
      queued: String,
      preCommand: String,
      main: String,
      @generic.extras.JsonKey("stage-in") stageIn: String
  )

}
