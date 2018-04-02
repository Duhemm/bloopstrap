package bloopstrap

import java.io.IOException
import java.nio.file.Files

import bloop.{DependencyResolution, ScalaInstance}
import bloop.integrations.BloopConfig
import bloop.io.AbsolutePath
import bloop.exec.JavaEnv

sealed trait Command {
  def process(state: State): State
}

final case class New(project: ProjectDescription) extends Command {
  override def process(state: State): State = {
    val projectBase = state.baseDirectory.resolve(project.name)
    val sourceBase = projectBase
      .resolve("src")
      .resolve(if (project.kind == PlainProject) "main" else "test")
    val configDir = state.baseDirectory.resolve(".bloop-config")
    val scalaInstance =
      ScalaInstance.resolve(project.scala.org,
                            project.scala.name,
                            project.scala.version,
                            state.logger)
    val javaEnv = project.javaHome match {
      case Some(home) =>
        JavaEnv(AbsolutePath(home), project.javaOptions.toArray)
      case None =>
        JavaEnv.default.copy(javaOptions = project.javaOptions.toArray)
    }
    val classpathOptions = {
      project.classpathOptions match {
        case Seq(bootLibrary, compiler, extra, autoBoot, filterLibrary) =>
          bloop.integrations.ClasspathOptions(bootLibrary,
                                              compiler,
                                              extra,
                                              autoBoot,
                                              filterLibrary)
        case _ =>
          val default = xsbti.compile.ClasspathOptionsUtil.boot
          bloop.integrations.ClasspathOptions(default.bootLibrary,
                                              default.compiler,
                                              default.extra,
                                              default.autoBoot,
                                              default.filterLibrary)
      }
    }

    val newProject =
      BloopConfig(
        name = project.name,
        baseDirectory = projectBase.toFile,
        dependencies = Seq.empty,
        scalaOrganization = scalaInstance.organization,
        scalaName = scalaInstance.name,
        scalaVersion = scalaInstance.version,
        classpath = Seq.empty,
        classpathOptions = classpathOptions,
        classesDir = projectBase.resolve("target").resolve("classes").toFile,
        scalacOptions = project.scalacOptions,
        javacOptions = project.javacOptions,
        sourceDirectories = Seq(sourceBase.resolve("scala").toFile,
                                sourceBase.resolve("java").toFile),
        testFrameworks =
          if (project.kind == PlainProject) Seq.empty
          else Defaults.defaultTestFrameworks,
        javaHome = javaEnv.javaHome.toFile,
        javaOptions = javaEnv.javaOptions,
        allScalaJars = scalaInstance.allJars,
        tmp = projectBase.resolve("tmp").toFile
      )

    state.copy(projects = state.projects + (project.name -> newProject))
  }
}
final case class DependsOn(projectName: String, dependency: String)
    extends Command {
  override def process(state: State): State = {
    (state.projects.get(projectName), state.projects.get(dependency)) match {
      case (Some(dependent), Some(dependee)) =>
        val newProject =
          dependent.copy(
            dependencies = dependent.dependencies :+ dependency,
            classpath = (dependent.classpath :+ dependee.classesDir) ++ dependee.classpath)
        state.copy(projects = state.projects + (projectName -> newProject))
      case (Some(_), None) =>
        state.logger.error(s"Not found: $dependency")
        state
      case (None, Some(_)) =>
        state.logger.error(s"Not found: $projectName")
        state
      case (None, None) =>
        state.logger.error(s"Not found: $projectName, $dependency")
        state
    }
  }
}
final case class AddLibrary(projectName: String, lib: ModuleDescriptor)
    extends Command {
  override def process(state: State): State = {
    state.projects.get(projectName) match {
      case Some(project) =>
        val dependencyFiles =
          DependencyResolution.resolve(lib.org,
                                       lib.name,
                                       lib.version,
                                       state.logger)

        val jars =
          dependencyFiles.filter(_.toString.endsWith(".jar")).map(_.toFile)
        val newProject =
          project.copy(classpath = project.classpath ++ jars)
        state.copy(projects = state.projects + (projectName -> newProject))
      case None =>
        state.logger.error(s"Not found: $projectName")
        state
    }
  }
}

final case object Generate extends Command {
  override def process(state: State): State = {
    val outBase = state.baseDirectory.resolve(".bloop-config")
    Files.createDirectories(outBase.underlying)
    state.projects.values.foreach { p =>
      val outFile = outBase.resolve(p.name + ".config").toFile
      p.writeTo(outFile)
      state.logger.info(
        s"Bloopstrap wrote config of project ${p.name} to $outFile")
    }
    state
  }
}

object Command {

  def makeTestProjects(projects: Map[String, ProjectDescription])
    : Map[String, ProjectDescription] = {
    projects.collect {
      case (k, v) if !projects.contains(k + "-test") =>
        val testProject =
          v.copy(name = v.name + "-test",
                 projectDependencies = v.projectDependencies :+ v.name,
                 kind = TestProject)
        testProject.name -> testProject
    }
  }

  def fromProjectDescriptions(
      projects: Map[String, ProjectDescription]): List[Command] = {
    val testProjects = makeTestProjects(projects)
    val descriptions = (projects ++ testProjects).values.toList
    val newCommands = descriptions.map { New(_) }
    val addLibCommands = descriptions.flatMap { desc =>
      desc.dependencies.map { dep =>
        AddLibrary(desc.name, dep)
      }
    }
    val dependsOnCommands = descriptions.flatMap { desc =>
      desc.projectDependencies.map { dep =>
        DependsOn(desc.name, dep)
      }
    }

    newCommands ++ addLibCommands ++ dependsOnCommands ++ (Generate :: Nil)
  }
}
