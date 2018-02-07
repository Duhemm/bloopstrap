import sbt._

object Dependencies {
  val bloopVersion = "1.0.0-M1"
  val metaconfigVersion = "0.6.0"

  val bloopFrontend = "ch.epfl.scala" %% "bloop-frontend" % bloopVersion
  val metaconfig = "com.geirsson" %% "metaconfig-typesafe-config" % metaconfigVersion
}
