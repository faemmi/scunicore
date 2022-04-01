package hpc.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import hpc.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting `jobs` to list all jobs.
  *
  * @param urls URLs of the jobs
  */
@generic.extras.ConfiguredJsonCodec
case class Jobs(
    @generic.extras.JsonKey("jobs") urls: List[String]
)
