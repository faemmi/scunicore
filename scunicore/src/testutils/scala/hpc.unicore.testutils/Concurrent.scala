object Concurrent {

  /** Returns a [[Future]] of any given object. */
  def asFuture[A](any: A)(implicit ec: ExecutionContext): Future[A] =
    Future { any }

  /** Await a Future. */
  def await[A](result: Future[A], maxWaitTime: Int = 1): A =
    Await.result(result, maxWaitTime.seconds)
}
