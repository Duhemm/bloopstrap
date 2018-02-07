package bloopstrap

import metaconfig.Conf
import metaconfig.Configured
import metaconfig.typesafeconfig._

import java.io.File

object ConfigReader {
  def apply(file: File): Configured[Map[String, ProjectDescription]] = {

    def parseDescList(lst: List[Conf]) = {
      val projects = lst.map(_.as[ConfigurationObject])
      Configured
        .traverse(projects)
        .map(_.collect {
          case desc: ProjectDescription => desc.name -> desc
        }.toMap)
    }

    Conf.parseFile(file).andThen {
      case Conf.Lst(values) =>
        parseDescList(values)

      case obj: Conf.Obj =>
        parseDescList(obj.map.values.toList)

      case other =>
        Configured.typeMismatch("Obj or List", other)
    }
  }
}
