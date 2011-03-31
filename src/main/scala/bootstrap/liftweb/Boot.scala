package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.common.{Loggable, Logger}
import zapush.{Scheduler, Config}

class Boot extends Loggable {
  def boot() {
    LiftRules.htmlProperties.default.set((r: Req) =>new Html5Properties(r.userAgent))

    LiftRules.addToPackages("zapush")

    logger.info("zapush initialised")
    logger.info(Config.toString)

    Scheduler.start()
    LiftRules.unloadHooks.append(() => Scheduler.stop())
  }
}