package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.common.{Loggable, Logger}
import zapush.{Scheduler, Config}

class Boot extends Loggable {
  def boot() {
    // generate html as html5
    LiftRules.htmlProperties.default.set((r: Req) =>new Html5Properties(r.userAgent))

    LiftRules.addToPackages("zapush")

    logger.info(Config.toString)

    if (Config.zabbix.isEmpty) {
      logger.error("Zabbix configuration incomplete; no data will be sent to zabbix")
    }

    logger.info("zapush initialised")

    Scheduler.start()
    LiftRules.unloadHooks.append(() => Scheduler.stop())
  }
}