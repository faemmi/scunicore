object Json {
  def decodeJsonString[A](json: String)(implicit decoder: circe.Decoder[A]): A =
    parser.decode[A](multiLineStringToJson(json)) match {
      case Left(error) => throw error
      case Right(res)  => res
    }

  def multiLineStringToJson(json: String): String = {
    json.replace(" ", "").replace("\n", "")
  }
}
