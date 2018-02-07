package bloopstrap

import scala.util.Try

import metaconfig.{Conf, ConfDecoder, Configured}

case class ModuleDescriptor(val org: String,
                            val name: String,
                            val version: String)

object ModuleDescriptor {

  def apply(descriptor: String): ModuleDescriptor = {
    descriptor.split(":") match {
      case Array(org, name, version) => new ModuleDescriptor(org, name, version)
      case _                         => throw new IllegalArgumentException(descriptor)
    }
  }

  def unapply(in: String): Option[ModuleDescriptor] =
    Try { ModuleDescriptor(in) }.toOption

  implicit val decoder: ConfDecoder[ModuleDescriptor] = {
    case Conf.Str(ModuleDescriptor(desc)) => Configured.Ok(desc)
    case other                            => Configured.typeMismatch("ModuleDescriptor", other)
  }
}
