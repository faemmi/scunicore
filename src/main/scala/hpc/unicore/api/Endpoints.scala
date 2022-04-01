package hpc.unicore.api

/** API endpoints of the UNICORE REST API.
  *
  * For details see https://sourceforge.net/p/unicore/wiki/REST_API/
  */
private[unicore] object Endpoints {

  /** The API endpoints for job interaction. */
  private[unicore] object Jobs {
    val base = "jobs"
    def baseForSubmittedJob(id: String): String = s"$base/$id"

    /** Submit a job. Requires a `POST` request. */
    val submit: String = base

    /** List all jobs. Requires a `GET` request. */
    def list(tags: List[String] = Nil): String = {
      val listAll = s"$base?offset=0"
      tags match {
        case Nil => listAll
        case _   => s"$listAll&tags=${tags.mkString(",")}"
      }
    }

    /** Get job properties. Requires a `GET` request to the base URL of a job. */
    val properties: String => String = baseForSubmittedJob

    /** Start a job. Requires a `POST` request. */
    def start(id: String): String = s"${baseForSubmittedJob(id)}/actions/start"

    /** Delete a job. Requires a `DELETE` request to the base URL of a job. */
    val delete: String => String = baseForSubmittedJob

    /** Abort a job. Requires a `POST` request. */
    def abort(id: String): String = s"${baseForSubmittedJob(id)}/actions/abort"
  }

  /** The API endpoints for WorkingDirectory interaction. */
  private[unicore] object WorkingDirectory {

    /** List all files. Requires a `GET` request. */
    val files: String = "files"
  }
}
