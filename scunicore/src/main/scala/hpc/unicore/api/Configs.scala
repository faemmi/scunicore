package ai.mantik.executor.hpc.api.unicore.api

import io.circe.generic.extras

/** Required for custom key names. */
object Configs {
  implicit val useDefaultValues = extras.Configuration.default.withDefaults
}
