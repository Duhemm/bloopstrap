package bloopstrap

import java.io.File
import metaconfig.{Conf, ConfDecoder, Configured}

sealed trait ConfigurationObject
case object NotAProject extends ConfigurationObject
case class ProjectDescription(name: String,
                              scala: ModuleDescriptor,
                              scalacOptions: Array[String],
                              javacOptions: Array[String],
                              fork: Boolean,
                              javaHome: Option[String],
                              javaOptions: Array[String],
                              dependencies: Array[ModuleDescriptor],
                              projectDependencies: Array[String],
                              kind: ProjectKind)
    extends ConfigurationObject

object ConfigurationObject {

  private implicit class FlatMapConfigured[T](val c: Configured[T])
      extends AnyVal {
    def flatMap[U](op: T => Configured[U]): Configured[U] = c match {
      case Configured.Ok(t)      => op(t)
      case err: Configured.NotOk => err
    }
  }

  implicit val decoder: ConfDecoder[ConfigurationObject] =
    new ConfDecoder[ConfigurationObject] {
      override def read(conf: Conf): Configured[ConfigurationObject] =
        conf match {
          case obj: Conf.Obj =>
            obj.field("module") match {
              case None =>
                Configured.Ok(NotAProject)
              case Some(moduleName) =>
                for {
                  name <- moduleName.as[String]
                  scala <- obj.getOrElse("scala")(Defaults.scala)
                  scalacOptions <- obj.getOrElse("scalacOptions")(
                    Defaults.scalacOptions)
                  javacOptions <- obj.getOrElse("javacOptions")(
                    Defaults.javacOptions)
                  fork <- obj.getOrElse("fork")(Defaults.fork)
                  javaHome <- obj.getOrElse("javaHome")(Defaults.javaHome)
                  javaOptions <- obj.getOrElse("javaOptions")(
                    Defaults.javaOptions)
                  dependencies <- obj.getOrElse("dependencies")(
                    Defaults.dependencies)
                  projectDependencies <- obj.getOrElse("projectDependencies")(
                    Defaults.projectDependencies)
                  kind = PlainProject
                  project = ProjectDescription(name,
                                               scala,
                                               scalacOptions,
                                               javacOptions,
                                               fork,
                                               javaHome,
                                               javaOptions,
                                               dependencies,
                                               projectDependencies,
                                               kind)
                } yield project
            }
          case _ =>
            Configured.typeMismatch("Obj", conf)
        }
    }
}
