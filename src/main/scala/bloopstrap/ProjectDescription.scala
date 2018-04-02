package bloopstrap

import java.io.File
import metaconfig.{Conf, ConfDecoder, Configured}

sealed trait ConfigurationObject
case object NotAProject extends ConfigurationObject
case class ProjectDescription(name: String,
                              classpathOptions: Seq[Boolean],
                              scala: ModuleDescriptor,
                              scalacOptions: Seq[String],
                              javacOptions: Seq[String],
                              fork: Boolean,
                              javaHome: Option[String],
                              javaOptions: Seq[String],
                              dependencies: Seq[ModuleDescriptor],
                              projectDependencies: Seq[String],
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
                  classpathOptions <- obj.getOrElse("classpathOptions")(
                    Defaults.classpathOptions)
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
                                               classpathOptions,
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
