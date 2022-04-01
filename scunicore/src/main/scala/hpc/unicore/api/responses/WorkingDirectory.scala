package ai.mantik.executor.hpc.api.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import ai.mantik.executor.hpc.api.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting a working directory. */
@generic.extras.ConfiguredJsonCodec
case class WorkingDirectory(
    @generic.extras.JsonKey("_links") links: WorkingDirectory.Links,
    @generic.extras.JsonKey("resourceStatus") status: String
)

object WorkingDirectory {

  /** URLS to API endpoints that allow to interact with the directory.
    *
    * @param base URL to the directory.
    * @param rename URL to rename the directory.
    * @param copy URL to copy the whole directory.
    * @param files URL to access the files/directory in the directory.
    */
  @generic.extras.ConfiguredJsonCodec
  case class Links(
      @generic.extras.JsonKey("self") base: Link,
      @generic.extras.JsonKey("action:rename") rename: Link,
      @generic.extras.JsonKey("action:copy") copy: Link,
      files: Link
  )
}
