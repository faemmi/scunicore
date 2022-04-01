package hpc.unicore

import com.typesafe.config.ConfigFactory
import org.scalatest

class ConfigSpec extends scalatest.FlatSpec with scalatest.Matchers {
  "The config" should "be creatable from the reference config" in {
    val path = "reference.conf"
    val config = ConfigFactory.parseResources(path)
    val result = Config.fromTypesafeConfig(config)

    result.registry.nonEmpty should be(true)
    result.resources.memory should be(None)
  }
}
