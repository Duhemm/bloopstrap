package bloopstrap

import bloop.io.AbsolutePath
import bloop.logging.BloopLogger
import metaconfig.Configured

object Bloopstrap {
  def main(args: Array[String]): Unit = {
    val logger = BloopLogger.default("bloopstrap")

    args match {
      case Array(configPath, destinationPath) =>
        logger.info(s"Reading configuration from $configPath")

        ConfigReader(AbsolutePath(configPath).toFile, Defaults.config) match {
          case Configured.Ok(descriptions) =>
            logger.info(
              s"Found ${descriptions.size} projects: ${descriptions.keys.mkString(", ")}")
            val commands = Command.fromProjectDescriptions(descriptions)

            logger.debug("Commands:")
            commands.foreach(c => logger.debug(c.toString))

            val initState = State.empty(logger, AbsolutePath(destinationPath))
            commands.foldLeft(initState)((state, cmd) => cmd.process(state))

          case Configured.NotOk(error) =>
            logger.error(error.toString)
            System.exit(1)
        }

      case _ =>
        logger.error("Expected input:")
        logger.error("bloopstrap <configuration-file> <destination>")
    }
  }
}
