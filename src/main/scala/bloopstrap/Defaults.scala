package bloopstrap

object Defaults {
  val defaultScalaOrganization = "org.scala-lang"
  val defaultScalaArtifact = "scala-compiler"
  val defaultScalaVersion = "2.12.4"
  val defaultScalaDependency =
    ModuleDescriptor(defaultScalaOrganization,
                     defaultScalaArtifact,
                     defaultScalaVersion)
  val scala = defaultScalaDependency
  val scalacOptions: Array[String] = Array.empty
  val javacOptions: Array[String] = Array.empty
  val fork: Boolean = true
  val javaHome: Option[String] = None
  val javaOptions: Array[String] = Array.empty
  val dependencies: Array[ModuleDescriptor] = Array.empty
  val projectDependencies: Array[String] = Array.empty

  val scalaCheck = Array("org.scalacheck.ScalaCheckFramework")
  val specs2 = Array("org.specs2.runner.Specs2Framework",
                     "org.specs2.runner.SpecsFramework",
                     "org.specs.runner.SpecsFramework")
  val scalatest = Array("org.scalatest.tools.Framework",
                        "org.scalatest.tools.ScalaTestFramework")
  val junit = Array("com.novocode.junit.JUnitFramework")
  val defaultTestFrameworks = Array(scalaCheck, specs2, scalatest, junit)

}
