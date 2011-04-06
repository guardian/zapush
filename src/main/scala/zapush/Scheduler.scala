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

package zapush

import java.util.concurrent.{TimeUnit, ThreadFactory, Executors}




object Scheduler {
  private var service = Executors.newSingleThreadScheduledExecutor(TF)

  def start() {
    // only start scheduling if we have a valid zabbix context
    for (zabbix <- Config.zabbix) {
      service.scheduleAtFixedRate(cmd, 5, zabbix.pushIntervalSecs, TimeUnit.SECONDS)
    }
  }
  def stop() { service.shutdown() }

  lazy val cmd = new Runnable { def run() { Sender.sendNow() } }

  private object TF extends ThreadFactory {
    val threadFactory = Executors.defaultThreadFactory()
    def newThread(r: Runnable) : Thread = {
      val d: Thread = threadFactory.newThread(r)
      d setName "Zabbix Trapper"
      d setDaemon true
      d
    }
  }

}