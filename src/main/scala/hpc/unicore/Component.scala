package hpc.unicore

import com.typesafe.scalalogging.Logger

import scala.concurrent.Future

/** Base class for Components. */
abstract class Component {

  /** Typesafe logger. */
  protected final val logger: Logger = Logger(getClass)
  logger.trace("Initializing...")
}
