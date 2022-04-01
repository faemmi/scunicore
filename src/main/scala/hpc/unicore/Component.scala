package hpc.unicore

import com.typesafe.scalalogging

import scala.concurrent.Future

/** Base class for Components. */
abstract class Component {

  /** Typesafe logger. */
  protected final val logger: scalalogging.Logger = scalalogging.Logger(getClass)
  logger.trace("Initializing...")
}
