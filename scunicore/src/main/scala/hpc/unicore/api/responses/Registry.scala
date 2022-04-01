package ai.mantik.executor.hpc.api.unicore.api.responses

import io.circe.generic

// Required for custom key names
import ai.mantik.executor.hpc.api.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting `jobs` to list all jobs. */
@generic.JsonCodec
case class Registry(
    owner: String,
    currentTime: String,
    resourceStatus: String,
    entries: List[Registry.Entry],
    resourceStatusMessage: String,
    siteName: String,
    tags: List[String]
)

object Registry {
  @generic.extras.ConfiguredJsonCodec
  case class Entry(
      ServerIdentity: String,
      InterfaceNamespace: String,
      href: String,
      @generic.extras.JsonKey("type") apiType: String
  )
}
