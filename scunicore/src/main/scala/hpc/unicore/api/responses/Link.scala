package ai.mantik.executor.hpc.api.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import ai.mantik.executor.hpc.api.unicore.api.Configs.useDefaultValues

/** A URL to a specific API endpoint. */
@generic.extras.ConfiguredJsonCodec
case class Link(
    description: Option[String],
    @generic.extras.JsonKey("href") url: String
)
