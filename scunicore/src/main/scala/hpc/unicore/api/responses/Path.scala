package ai.mantik.executor.hpc.api.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import ai.mantik.executor.hpc.api.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting a path in a working directory.
  *
  * @param links Links related to the path.
  * @param owner Owner of the path.
  * @param group Group the path belongs to.
  * @param isDirectory Whether the path is a directory or a file.
  * @param size Size of the path in bytes.
  * @param content Content of the path.
  *                Only present if [[Path.isDirectory]] is `true`.
  */
@generic.extras.ConfiguredJsonCodec
case class Path(
    @generic.extras.JsonKey("_links") links: Path.Links,
    owner: String,
    group: String,
    isDirectory: Boolean,
    size: Int,
    content: Option[Map[String, Path.Element]]
)

object Path {

  /** URLS to API endpoints that allow to interact with the job via `POST` requests.
    *
    * @param base URL to the path itself.
    *             Only given if the path is a directory.
    * @param parentStorage Path of the parent storage directory.
    * @param next Path to the next files.
    */
  @generic.extras.ConfiguredJsonCodec
  case class Links(
      @generic.extras.JsonKey("self") base: Option[Link],
      @generic.extras.JsonKey("parentStorage") parentStorage: Link,
      @generic.extras.JsonKey("next") next: Option[Link]
  )

  /** An element in the content of a [[Path]].
    *
    * @param owner Owner of the path.
    * @param group Group the path belongs to.
    * @param isDirectory Whether the path is a directory or a file.
    * @param size Size of the path in bytes.
    * @param permissions UNIX-like permission representation.
    */
  @generic.extras.ConfiguredJsonCodec
  case class Element(
      owner: String,
      group: String,
      isDirectory: Boolean,
      size: Int,
      permissions: String
  )
}
