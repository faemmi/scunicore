package ai.mantik.executor.hpc.api.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import ai.mantik.executor.hpc.api.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting `jobs` to list all jobs.
  *
  * @param urls URLs of the jobs
  */
@generic.extras.ConfiguredJsonCodec
case class Jobs(
    @generic.extras.JsonKey("jobs") urls: List[String]
)
