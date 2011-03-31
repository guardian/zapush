package zapush

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import management.ManagementFactory
import javax.management.{MBeanAttributeInfo, ObjectName}
import scala.collection.JavaConversions._
import javax.management.openmbean._
import snippet.{MBeanProperty, MBean}

class MBeanTest extends FlatSpec with ShouldMatchers {
  val mbeanServer = ManagementFactory.getPlatformMBeanServer
  val testObject = new ObjectName("java.lang:type=MemoryPool,name=PS Perm Gen")
  val bean = new MBean(testObject)

  "mbean wrapper" should "read composite properties" in {
    val properties = bean.properties
    println("properties are : " + properties.toList.sortBy(_.name).map(_.name).mkString("\n"))
  }

  it should "be able to read composide propeties" in {
    println("bean name = " + bean("Name"))
    println("bean usage max = " + bean("Usage.max"))
  }

}