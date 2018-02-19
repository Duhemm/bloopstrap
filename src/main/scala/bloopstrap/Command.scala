package bloopstrap

import java.io.IOException
import java.nio.file.Files

import bloop.{DependencyResolution, Project, ScalaInstance}
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
        JavaEnv(project.fork, AbsolutePath(home), project.javaOptions)
      case None =>
        JavaEnv.default(project.fork).copy(javaOptions = project.javaOptions)
    }

    val newProject =
      Project(
        name = project.name + (if (project.kind == PlainProject) ""
                               else "-test"),
        baseDirectory = projectBase,
        dependencies = Array.empty,
        scalaInstance = scalaInstance,
        rawClasspath = Array.empty,
        classesDir = projectBase.resolve("target").resolve("classes"),
        scalacOptions = project.scalacOptions,
        javacOptions = project.javacOptions,
        sourceDirectories =
          Array(sourceBase.resolve("scala"), sourceBase.resolve("java")),
        testFrameworks =
          if (project.kind == PlainProject) Array.empty
          else Defaults.defaultTestFrameworks,
        javaEnv = javaEnv,
        tmp = projectBase.resolve("tmp"),
        bloopConfigDir = configDir
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
            rawClasspath = (dependent.rawClasspath :+ dependee.classesDir) ++ dependee.rawClasspath)
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

        val jars = dependencyFiles.filter(_.toString.endsWith(".jar"))
        val newProject =
          project.copy(rawClasspath = project.rawClasspath ++ jars)
        state.copy(projects = state.projects + (projectName -> newProject))
      case None =>
        state.logger.error(s"Not found: $projectName")
        state
    }
  }
}

final case object Generate extends Command {
  override def process(state: State): State = {
    state.projects.values.foreach { p =>
      val props = p.toProperties
      val outPath = p.bloopConfigDir.resolve(p.name + ".config")
      try {
        Files.createDirectories(p.bloopConfigDir.underlying)
        val stream = Files.newOutputStream(outPath.underlying)
        props.store(stream, null)
        state.logger.info(
          s"Bloopstrap wrote config of project ${p.name} to $outPath")
      } catch {
        case ex: IOException =>
          state.logger.error(s"Writing to $outPath failed: ${ex.getMessage}")
          state.logger.trace(ex)
      }
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
          v.copy(projectDependencies = v.projectDependencies :+ v.name,
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
