/*
 * Copyright (c) 2011 Guardian Media Group
 *
 * This file is part of Zapush.
 *
 * Zapush is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Zapush is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

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