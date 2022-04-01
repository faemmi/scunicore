package hpc.unicore

import com.typesafe.scalalogging

/** Base class for Components. */
abstract class Component {

  /** Typesafe logger. */
  protected final val logger: scalalogging.Logger = scalalogging.Logger(getClass)
  logger.trace("Initializing...")
}
