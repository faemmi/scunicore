addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4")

// https://scalapb.github.io/sbt-settings.html
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.3")
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.10.11"
)

addSbtPlugin("au.com.onegeek" %% "sbt-dotenv" % "2.0.117")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.3")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.3")
addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings" % "3.0.0")
