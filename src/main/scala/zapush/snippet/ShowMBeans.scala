package zapush.snippet

import net.liftweb.util.Helpers._
import xml.Text
import zapush.MBeans

class ShowMBeans {
  def render = "li" #> MBeans.all.flatMap(_.properties).sortBy(_.zabbixName).map { prop =>
      ".prop-name *" #> prop.zabbixName &
      ".prop-type" #> prop.attributeType &
      ".prop-value" #> tryo { Text(prop.value.toString) }.openOr(<strong>error!</strong>)
  }
}