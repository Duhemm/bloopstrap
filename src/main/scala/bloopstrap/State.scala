package bloopstrap

import bloop.integrations.BloopConfig
import bloop.io.AbsolutePath
import bloop.logging.{BloopLogger, Logger}

final case class State(logger: Logger,
                       baseDirectory: AbsolutePath,
                       projects: Map[String, BloopConfig])

object State {
  def empty(logger: Logger, base: AbsolutePath): State =
    State(logger, base, Map.empty)
}
