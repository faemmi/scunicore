package ai.mantik.executor.hpc.api.unicore.api

import ai.mantik.componently.{AkkaRuntime, ComponentBase}
import ai.mantik.executor.hpc.api.http

import scala.concurrent.Future

/** A given registry does not provide any API URLs of computation sites. */
case class NoSitesAvailableException(message: String) extends Exception

/** Represents a registry containing UNICORE API URLs of computation sites.
  *
  * @constructor Create a new registry with a certain URL.
  * @param httpClient Must contain the URL to the registry.
  */
class Registry(httpClient: http.Client)(implicit akkaRuntime: AkkaRuntime)
    extends ComponentBase
    with Resource[responses.Registry] {
  protected val client: http.Client = httpClient.withJsonResponseHeader()

  override def toString: String = s"${getClass.getName}(url = ${client.baseUrl})"

  override def properties(): Future[responses.Registry] = {
    logger.debug(s"Getting properties for $this")
    val response = client.sendRequestAndDecodeResponseToString()
    Decoder.decodeJsonString[responses.Registry](response)
  }

  /** Returns the API URL for a given site name.
    *
    * @param site Name of the site.
    *
    * @throws RuntimeException if the site is not available in the registry.
    *
    * @return API URL of the site.
    */
  def getSiteApiUrl(site: String): Future[String] = {
    logger.debug(s"Getting URL for site $site from $this")
    siteApiUrls().map(apiUrls => {
      val apiUrl = apiUrls.getOrElse(
        site,
        throw new RuntimeException(s"$site not available in $this. Available clusters: ${apiUrls.keys}")
      )
      logger.debug(s"Site URL for $site is $apiUrl")
      apiUrl
    })
  }

  /** Returns the API URLs of each site the registry provides.
    *
    * @return Keys: site names, values: respective API URL.
    */
  private def siteApiUrls(): Future[Map[String, String]] = {
    logger.debug(s"Requesting available sites from $this")
    val response = properties()
    providesSiteApis(response).flatMap(
      {
        case true =>
          val apiUrls = getApiUrlsForAllAvailableSites(response)
          logger.debug(s"Got API URLs from $this: $apiUrls")
          apiUrls
        case false => throw NoSitesAvailableException(s"$this does not provide any site APIs")
      }
    )
  }

  /** Check if the registry provides any API URLs. */
  private def providesSiteApis(response: Future[responses.Registry]): Future[Boolean] =
    response.map(_.entries.nonEmpty)

  private def getApiUrlsForAllAvailableSites(response: Future[responses.Registry]): Future[Map[String, String]] =
    response.map(registry =>
      Registry.getSiteApiUrls(registry.entries).map(url => (Registry.getSiteNameFromApiUrl(url), url)).toMap
    )

}

object Registry {
  // This is the `type` of the API URL of a computation site.
  private val CoreApiType = "CoreServices"
  // Typically, an API URL looks like `https://<address>:<port>/<site name in capital letters>/rest/core`.
  private val CoreApiUrlPattern = ".*//.*/(\\w+)/rest/core$".r

  /** Returns the API URLs of the UNICORE API of each site available in the registry. */
  private def getSiteApiUrls(entries: List[responses.Registry.Entry]): List[String] =
    for (entry <- entries if entry.apiType == CoreApiType) yield entry.href

  /** Returns the name of a site from its API URL. */
  private def getSiteNameFromApiUrl(url: String): String = url match {
    case CoreApiUrlPattern(siteName) => siteName
    case _                           => throw new RuntimeException(s"Could not find a site name in $url")
  }
}
