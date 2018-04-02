package metaconfig

import com.typesafe.config.Config
import scala.meta.inputs.Input

// Copy-pasted from metaconfig
// Added support for fallback configs
package object typesafeconfig {
  def typesafeConfigMetaconfigParser(fallback: Config) = new MetaconfigParser {
    override def fromInput(input: Input): Configured[Conf] = input match {
      case Input.File(path, _) =>
        TypesafeConfig2Class.gimmeConfFromFile(path.toFile, fallback)
      case els =>
        TypesafeConfig2Class.gimmeConfFromString(new String(els.chars),
                                                 fallback)
    }
  }
}
