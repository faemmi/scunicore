package hpc.unicore.api

import scala.concurrent.{Future, ExecutionContext}
import hpc.unicore.http

/** A resource representing a UNICORE REST API concept/object. */
trait Resource[T] {
  implicit protected def ec: ExecutionContext
  protected val client: http.Client

  def properties(): Future[T]

  override def toString: String = s"${getClass.getName}(url = ${client.baseUrl})"
}
