import sbt._

object Dependencies {
  val bloopVersion = "1.0.0-M8"
  val metaconfigVersion = "0.6.0"

  val bloopFrontend = "ch.epfl.scala" %% "bloop-frontend" % bloopVersion
  val bloopIntegrationsCore = "ch.epfl.scala" %% "bloop-integrations-core" % bloopVersion
  val metaconfig = "com.geirsson" %% "metaconfig-typesafe-config" % metaconfigVersion
}
