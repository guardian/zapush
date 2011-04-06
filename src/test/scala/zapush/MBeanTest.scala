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

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import management.ManagementFactory
import javax.management.{MBeanAttributeInfo, ObjectName}
import scala.collection.JavaConversions._
import javax.management.openmbean._

class MBeanTest extends FlatSpec with ShouldMatchers {
  val mbeanServer = ManagementFactory.getPlatformMBeanServer
  val testObject = new ObjectName("java.lang:type=MemoryPool,name=CMS Perm Gen")
  val bean = new MBean(testObject)

  "mbean wrapper" should "read composite properties" in {
    val properties = bean.properties
    println("properties are : " + properties.toList.sortBy(_.propertyName).map(_.propertyName).mkString("\n"))
  }

  it should "be able to read composide propeties" in {
    println("bean name = " + bean("Name"))
    println("bean usage max = " + bean("Usage.max"))
  }

//  it should "be able to read some crazy properties" in {
//    val gc = new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep")
//    val gcBean = new MBean(gc)
//
//    val actualValue = gcBean("LastGcInfo.memoryUsageAfterGc.CMS Old Gen.init")
//    println("actual value = " + actualValue.toString)
//
//  }
//
//
//  it should "return crazy properties in the list of properties" in {
//    val gc = new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep")
//    val gcBean = new MBean(gc)
//
//    (gcBean.properties).find(_.propertyName == "LastGcInfo.memoryUsageAfterGc.CMS Old Gen.init") should be ('defined)
//
//
//  }



}