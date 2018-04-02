package bloopstrap

import com.typesafe.config.ConfigFactory

object Defaults {
  val defaultScalaOrganization = "org.scala-lang"
  val defaultScalaArtifact = "scala-compiler"
  val defaultScalaVersion = "2.12.4"
  val defaultScalaDependency =
    ModuleDescriptor(defaultScalaOrganization,
                     defaultScalaArtifact,
                     defaultScalaVersion)
  val scala = defaultScalaDependency
  val classpathOptions: Seq[Boolean] = Seq.empty
  val scalacOptions: Seq[String] = Seq.empty
  val javacOptions: Seq[String] = Seq.empty
  val fork: Boolean = true
  val javaHome: Option[String] = None
  val javaOptions: Seq[String] = Seq.empty
  val dependencies: Seq[ModuleDescriptor] = Seq.empty
  val projectDependencies: Seq[String] = Seq.empty

  val scalaCheck = Seq("org.scalacheck.ScalaCheckFramework")
  val specs2 = Seq("org.specs2.runner.Specs2Framework",
                   "org.specs2.runner.SpecsFramework",
                   "org.specs.runner.SpecsFramework")
  val scalatest = Seq("org.scalatest.tools.Framework",
                      "org.scalatest.tools.ScalaTestFramework")
  val junit = Seq("com.novocode.junit.JUnitFramework")
  val defaultTestFrameworks = Seq(scalaCheck, specs2, scalatest, junit)

  val config =
    ConfigFactory.parseString("""defaults {
      |  scalaCompiler = "org.scala-lang:scala-compiler"
      |}""".stripMargin)

}
