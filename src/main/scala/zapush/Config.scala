package zapush

import net.liftweb.common._
import net.liftweb.util.Helpers._
import java.net.InetAddress

case class ZabbixConfig(
  server: String,
  serverPort: Int,
  hostname: String,
  appname: String,
  pushIntervalSecs: Int
)

object Config {
  private def systemProperty(propertyName: String) =
    Box.legacyNullTest(System.getProperty(propertyName)) ?~ ("System property '" + propertyName + "' missing")

  private def thisHostname = tryo { InetAddress.getLocalHost.getHostName }

  lazy val zabbix = for {
    server <- systemProperty("zabbix.server")
    hostname <- systemProperty("zabbix.hostname") or thisHostname
    val serverPort = systemProperty("zabbix.server.port") openOr "10051" toInt
    val appname = systemProperty("zabbix.appname") openOr "zapush"
    val pushIntervalSeconds = systemProperty("zabbix.push.interval.secs") openOr "60" toInt
  } yield {
    ZabbixConfig(server, serverPort, hostname, appname, pushIntervalSeconds)
  }

  override def toString = "zabbix config: " + zabbixConfigToString

  lazy val zabbixConfigToString = zabbix match {
    case f: Failure => "INVALID: " + f.messageChain
    case Full(z) => "server = %s:%d, this hostname = %s, this app name = %s, push interval = %d seconds"
       .format(z.server, z.serverPort, z.hostname, z.appname, z.pushIntervalSecs)
    case Empty => "INVALID: unknown reason"
  }

  lazy val zabbixAppname = zabbix.map(_.appname) openOr "zapush"


}