package ai.mantik.executor.hpc.api.unicore.api

import scala.concurrent.Future
import ai.mantik.executor.hpc.api.http

/** A resource representing a UNICORE REST API concept/object. */
trait Resource[T] {
  protected val client: http.Client

  def properties(): Future[T]

  override def toString: String = s"${getClass.getName}(url = ${client.baseUrl})"
}
