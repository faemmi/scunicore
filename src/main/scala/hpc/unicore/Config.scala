package hpc.unicore

import scala.util.Try

/** UNICORE-specific config settings. */
case class Config(
    registry: String,
    site: SiteConfig,
    login: LoginConfig,
    accounting: AccountingConfig,
    resources: ResourcesConfig
)

object Config {
  def fromTypesafeConfig(config: com.typesafe.config.Config): Config = {
    Config(
      registry = config.getString("unicore.registry"),
      site = SiteConfig.fromTypesafeConfig(config),
      login = LoginConfig.fromTypesafeConfig(config),
      accounting = AccountingConfig.fromTypesafeConfig(config),
      resources = ResourcesConfig.fromTypesafeConfig(config)
    )
  }
}

case class SiteConfig(name: String)

object SiteConfig {
  def fromTypesafeConfig(config: com.typesafe.config.Config): SiteConfig = {
    SiteConfig(
      name = config.getString("unicore.site.name")
    )
  }
}

case class LoginConfig(
    user: String,
    password: String
)

object LoginConfig {
  def fromTypesafeConfig(config: com.typesafe.config.Config): LoginConfig = {
    LoginConfig(
      user = config.getString("unicore.login.user"),
      password = config.getString("unicore.login.password")
    )
  }
}

case class AccountingConfig(project: String)

object AccountingConfig {
  def fromTypesafeConfig(config: com.typesafe.config.Config): AccountingConfig = {
    AccountingConfig(
      project = config.getString("unicore.accounting.project")
    )
  }
}

case class ResourcesConfig(
    queue: String,
    nodes: Int,
    cpus: Option[Int],
    cpusPerNode: Option[Int],
    memory: Option[String],
    reservation: Option[String],
    nodeConstraints: Option[String]
)

object ResourcesConfig {
  def fromTypesafeConfig(config: com.typesafe.config.Config): ResourcesConfig = {
    ResourcesConfig(
      queue = config.getString("unicore.resources.queue"),
      nodes = config.getInt("unicore.resources.nodes"),
      cpus = Getter.getOption[Int](config.getInt)("unicore.resources.cpus"),
      cpusPerNode = Getter.getOption[Int](config.getInt)("unicore.resources.cpusPerNode"),
      memory = Getter.getOption[String](config.getString)("unicore.resources.memory"),
      reservation = Getter.getOption[String](config.getString)("unicore.resources.reservation"),
      nodeConstraints = Getter.getOption[String](config.getString)(
        "unicore.resources.nodeConstraints"
      )
    )
  }
}

object Getter {
  private[hpc] def getOption[T](get: String => T)(path: String): Option[T] =
    Try(path).map(get).map(Some(_)).getOrElse(None)
}
