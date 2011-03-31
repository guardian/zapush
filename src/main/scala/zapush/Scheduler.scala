package zapush

import java.util.concurrent.{TimeUnit, ThreadFactory, Executors}




object Scheduler {
  private var service = Executors.newSingleThreadScheduledExecutor(TF)

  def start() { service.scheduleAtFixedRate(cmd, 5, Config.zabbixPushIntervalSeconds, TimeUnit.SECONDS) }
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