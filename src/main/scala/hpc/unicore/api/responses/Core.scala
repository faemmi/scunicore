package hpc.unicore.api.responses

import io.circe.generic

// Required for custom key name.
import hpc.unicore.api.Configs.useDefaultValues

/** UNICORE API response when `GET` requesting job properties. */
@generic.JsonCodec
case class Core(server: Core.Server, client: Core.Client) {
  val loggedIn: Boolean = client.login.Successful
}

object Core {

  @generic.JsonCodec
  case class Server(jobSubmission: JobSubmission, version: String)

  @generic.JsonCodec
  case class JobSubmission(enabled: Boolean, message: String)

  @generic.extras.ConfiguredJsonCodec
  case class Client(
      role: Role,
      queues: Queues,
      @generic.extras.JsonKey("xlogin") login: Login
  )

  @generic.JsonCodec
  case class Role(availableRoles: List[String], selected: String)

  @generic.JsonCodec
  case class Queues(availableQueues: List[String], selected: String)

  @generic.extras.ConfiguredJsonCodec
  case class Login(
      @generic.extras.JsonKey("UID") user: Option[String],
      availableGroups: Option[List[String]],
      @generic.extras.JsonKey("availableUIDs") availableUserIds: Option[List[String]],
      group: Option[String]
  ) {
    private[responses] val Successful: Boolean =
      user.isDefined &&
        availableGroups.isDefined &&
        availableUserIds.isDefined &&
        group.isDefined
  }
}
