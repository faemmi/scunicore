package hpc.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import hpc.unicore.api.Configs.useDefaultValues

/** A URL to a specific API endpoint. */
@generic.extras.ConfiguredJsonCodec
case class Link(
    description: Option[String],
    @generic.extras.JsonKey("href") url: String
)
