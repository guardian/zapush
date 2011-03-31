package zapush



object Config {
  lazy val zabbixServer = mandatorySystemProperty("zabbix.server")
  lazy val zabbixServerPort = System.getProperty("zabbix.push.interval.secs", "10051").toInt
  lazy val zabbixHostname = mandatorySystemProperty("zabbix.hostname")
  lazy val zabbixAppname = mandatorySystemProperty("zabbix.appname")
  lazy val zabbixPushIntervalSeconds = System.getProperty("zabbix.push.interval.secs", "60").toInt

  def mandatorySystemProperty(propertyName: String) =
    Option(System.getProperty(propertyName)) getOrElse error("System property " + propertyName + " is required")


  override def toString =
    "zabbix server = %s port %d, this hostname = %s, this app name = %s, push interval = %d seconds"
      .format(Config.zabbixServer, Config.zabbixServerPort, Config.zabbixHostname, Config.zabbixAppname, Config.zabbixPushIntervalSeconds)


}