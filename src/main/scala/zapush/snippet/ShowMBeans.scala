package zapush.snippet

import net.liftweb.util.Helpers._
import management.ManagementFactory
import collection.JavaConversions._
import xml.Text
import zapush.{MBeans, MBean}

class ShowMBeans {
  def render = ".domain" #> domains.map { domainName =>
    ".domain-name" #> domainName &
    ".bean" #> mbeanNamesByDomain(domainName).map(new MBean(_)).sortBy(_.shortName).map { bean =>
      ".bean-name" #> bean.shortName &
      ".bean-property" #> bean.properties.toList.sortBy(_.propertyName).map { prop =>
        ".prop-name *" #> prop.propertyName &
        ".prop-type" #> prop.attributeType &
        ".prop-value" #> tryo { Text(bean(prop.propertyName)) }.openOr(<strong>error!</strong>)
      }
    }
  }

  lazy val mbeanNamesByDomain = MBeans.mbeanNames.groupBy(_.getDomain)
  lazy val domains = mbeanNamesByDomain.keys.toList.sorted


}