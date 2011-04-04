package zapush

import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._
import java.net.Socket
import net.liftweb.json._
import scalax.io.Resource
import scalax.io.Codec

object Sender extends Loggable {
  implicit val jsonFormat = DefaultFormats
  implicit val codec = Codec("ASCII")

  def sendNow() {
    logger.info("Sending latest data to zabbix...")

    try {
      val toSend = ZabbixSenderData(data = data)

      logger.debug("data to send = " + toSend.data.map(d => d.key + " -> " + d.value).mkString("\n"))

      val socket = new Socket(Config.zabbixServer, Config.zabbixServerPort)

      val m = ZabbixMessage(compact(render(Extraction.decompose(toSend))))
//      println("message is: " + m.message)
//      println("or: " + m.asBytes)
//
      logger.debug("writing to zabbix")
      socket.getOutputStream.write(m.asBytes.toArray)

      logger.debug("reading response")
      val input = Resource.fromInputStream(socket.getInputStream)
      val binaryResult: List[Byte] = input.byteArray.toList
      logger.debug("binary result: " + binaryResult)
      val result = ZabbixMessage.parse(binaryResult)
      val caseClassResult = Serialization.read[ZabbixSenderDataResponse](result)
      logger.info("result: " + caseClassResult)
    } catch {
      case e: Exception => logger.warn("sending failed", e)
    }

    logger.info("Sent latest data to zabbix")
  }

  def data: List[Data] =
    for {
      mbean <- MBeans.all
      property <- mbean.properties
      value <- tryo { mbean(property.propertyName) }
    } yield {
      Data(Config.zabbixHostname, property.zabbixName, value.toString)
    }
}

