package zapush

import management.ManagementFactory
import collection.JavaConversions._
import javax.management.openmbean._
import javax.management.{MBeanAttributeInfo, ObjectName}


object MBeans {
  lazy val mbeanServer = ManagementFactory.getPlatformMBeanServer
  def mbeanNames = mbeanServer.queryNames(null, null).toList
  def all = mbeanNames.map(new MBean(_))
}

case class MBeanProperty(bean: MBean, propertyName: String, attributeType: String) {
  lazy val objectName = bean.name
  lazy val zabbixName = "java[" + Config.zabbixAppname + ":" + objectName.toString + ":" + propertyName + "]"
  def value = bean(propertyName)
}

class MBean(val name: ObjectName) {
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
          List(MBeanProperty(this, path, "Array[" + arr.getElementOpenType.getTypeName + "]"))
        case other =>
          List(MBeanProperty(this, path, other.getTypeName))
      }

      attr match {
        case open: OpenMBeanAttributeInfo =>
          parseOpenType(attr.getName, open.getOpenType)
        case closed: MBeanAttributeInfo =>
          List(MBeanProperty(this, attr.getName, closed.getType))
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

