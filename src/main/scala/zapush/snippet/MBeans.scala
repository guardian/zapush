package zapush.snippet

import net.liftweb.util.Helpers._
import management.ManagementFactory
import collection.JavaConversions._
import xml.Text
import javax.management.openmbean._
import javax.management.{MBeanAttributeInfo, MBeanServer, ObjectName}
import annotation.tailrec

case class MBeanProperty(name: String, attributeType: String)

class MBean(name: ObjectName) {
  lazy val mbeanServer = ManagementFactory.getPlatformMBeanServer

  // naming convention:
  //  "attribute" refers to a jmx attribute
  //  "property" refers to our flattened name, dot separated, including the values within composite attributes

  lazy val readableAttributes =
    mbeanServer.getMBeanInfo(name).getAttributes.filter(_.isReadable).toSet

  lazy val compositeAttributes =
    readableAttributes.filter(_.getType == classOf[CompositeData].getName)

  lazy val topLevelReadableAttributes = readableAttributes -- compositeAttributes

  lazy val properties = readableAttributes flatMap { attr =>
      def parseOpenType(path: String, ot: OpenType[_]): List[MBeanProperty] = ot match {
        case c: CompositeType =>
          c.keySet.toList.flatMap(itemName => parseOpenType(path + "." + itemName, c.getType(itemName)))
        case arr: ArrayType[_] =>
          List(MBeanProperty(path, "Array[" + arr.getElementOpenType.getTypeName + "]"))
        case other =>
          List(MBeanProperty(path, other.getTypeName))
      }

      attr match {
        case open: OpenMBeanAttributeInfo =>
          parseOpenType(attr.getName, open.getOpenType)
        case closed: MBeanAttributeInfo =>
          List(MBeanProperty(attr.getName, closed.getType))
      }
    }

  lazy val shortName = name.getKeyPropertyListString
  lazy val fullName = name.toString

  def apply(attributeName: String) = {

    def getValue(attributeName: String): Any =
      attributeName.split('.').toList match {
        case Nil => error("bad attibute name: " + attributeName)
        case value :: Nil =>
          mbeanServer.getAttribute(name, value)
        case compositeName :: rest =>
          parseComposite(rest, mbeanServer.getAttribute(name, compositeName).asInstanceOf[CompositeData])
      }

    @tailrec
    def parseComposite(path: List[String], data: CompositeData): Any = path match {
      case Nil => error("erm something went wong")
      case key :: Nil => data.get(key)
      case key :: rest => parseComposite(rest, data.get(key).asInstanceOf[CompositeData])
    }

    getValue(attributeName) match {
      case a: Array[_] => a.mkString(", ")
      case m: Map[_, _] => m.mkString("map: ", ", ", "end")
      case m: java.util.Map[_, _] => m.mkString("java map: ", ", ", "end")
      case other => other.toString
    }
  }



}

class MBeans {
  def render = ".domain" #> domains.map { domainName =>
    ".domain-name" #> domainName &
    ".bean" #> mbeanNamesByDomain(domainName).map(new MBean(_)).sortBy(_.shortName).map { bean =>
      ".bean-name" #> bean.shortName &
      ".bean-property" #> bean.properties.toList.sortBy(_.name).map { prop =>
        ".prop-name *" #> prop.name &
        ".prop-type" #> prop.attributeType &
        ".prop-value" #> tryo { Text(bean(prop.name)) }.openOr(<strong>error!</strong>)
      }
    }
  }

  lazy val mbeanServer = ManagementFactory.getPlatformMBeanServer
  lazy val mbeanNames = mbeanServer.queryNames(null, null).toList

  lazy val mbeanNamesByDomain = mbeanNames.groupBy(_.getDomain)

  lazy val domains = mbeanNamesByDomain.keys.toList.sorted


}