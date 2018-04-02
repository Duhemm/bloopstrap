val bloopstrap = project.in(file("."))

organization := "com.github.duhemm"
name := "bloopstrap"
description := "Define and import Bloop projects without build tool"
libraryDependencies += Dependencies.bloopFrontend
libraryDependencies += Dependencies.bloopIntegrationsCore
libraryDependencies += Dependencies.metaconfig

inThisBuild(
  List(
    licenses := Seq(
      "apache-2.0" -> url("http://opensource.org/licenses/apache-2.0")),
    homepage := Some(url("https://github.com/Duhemm/bloopstrap")),
    developers := List(
      Developer("Duhemm",
                "Martin Duhem",
                "martin.duhem@gmail.com",
                url("https://github.com/Duhemm"))),
    scmInfo := Some(ScmInfo(url("https://github.com/Duhemm/bloopstrap"),
                            "scm:git:git@github.com:Duhemm/bloopstrap.git")),
    releaseEarlyWith := SonatypePublisher,
    resolvers += Resolver.bintrayRepo("scalameta", "maven")
  ))
