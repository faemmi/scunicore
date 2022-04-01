package hpc.unicore.api

import org.scalatest

class EndpointsSpec extends scalatest.FlatSpec with scalatest.Matchers {
  private val id1 = "test-id-1"
  private val id2 = "test-id-2"

  "The Endpoints class" should "create the API path for submitting a job" in {
    Endpoints.Jobs.submit should be("jobs")
  }

  it should "create the API path for listing all deployed jobs" in {
    Endpoints.Jobs.list() should be("jobs?offset=0")
  }

  it should "create the API path for listing all deployed jobs filtered by tags" in {
    Endpoints.Jobs.list(tags = List("tag1", "tag2")) should be("jobs?offset=0&tags=tag1,tag2")
  }

  it should "create several API paths for getting job properties" in {
    Endpoints.Jobs.properties(id1) should be(s"jobs/$id1")
    Endpoints.Jobs.properties("test-id-2") should be(s"jobs/$id2")
  }

  it should "create several API paths for deleting jobs" in {
    Endpoints.Jobs.properties(id1) should be(s"jobs/$id1")
    Endpoints.Jobs.properties("test-id-2") should be(s"jobs/$id2")
  }

  it should "create several API paths for aborting jobs" in {
    Endpoints.Jobs.abort(id1) should be(s"jobs/$id1/actions/abort")
    Endpoints.Jobs.abort(id2) should be(s"jobs/$id2/actions/abort")
  }

  it should "create the API path for listing all files in a directory" in {
    Endpoints.WorkingDirectory.files should be("files")
  }
}
