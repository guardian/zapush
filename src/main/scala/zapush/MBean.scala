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

case class MBeanValue(rawValue: AnyRef) {
  override def toString = rawValue match {
    case a: Array[_] => a.mkString(", ")
    case t: TabularData => "[tabular data not yet supported by zapush]"
    case m: Map[_, _] => m.mkString("map: ", ", ", "end")
    case other => other.toString
  }

  def child(childName: String): MBeanValue = {
    rawValue match {
      case c: CompositeData => MBeanValue(c.get(childName))
      case t: TabularData => error("tabular data not yet supported by zapush")
//        t.values
//          .collect { case cd: CompositeData if cd.get("key") == childName => MBeanValue(cd.get("value")) }
//          .headOption.getOrElse(error("failed to parse tabular data for " + childName + " from " + rawValue))
      case _ => error("could not find child named " + childName + " from " + rawValue)
    }
  }
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

    def parseComposite(path: List[String], data: MBeanValue): MBeanValue =
      path match {
        case Nil => error("erm something went wong")
        case key :: Nil => data.child(key)
        case key :: rest => parseComposite(rest, data.child(key))
      }

    attributeName.split('.').toList match {
      case Nil => error("bad attibute name: " + attributeName)
      case value :: Nil =>
        MBeanValue(mbeanServer.getAttribute(name, value))
      case compositeName :: rest =>
        parseComposite(rest, MBeanValue(mbeanServer.getAttribute(name, compositeName)))
    }
  }




}

