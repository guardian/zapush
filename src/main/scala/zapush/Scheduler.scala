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