package bloopstrap

import bloop.Project
import bloop.io.AbsolutePath
import bloop.logging.{BloopLogger, Logger}

final case class State(logger: Logger,
                       baseDirectory: AbsolutePath,
                       projects: Map[String, Project])

object State {
  def empty(logger: Logger, base: AbsolutePath): State =
    State(logger, base, Map.empty)
}
