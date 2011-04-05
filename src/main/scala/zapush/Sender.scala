package zapush

import net.liftweb.util.Helpers._
import java.net.Socket
import net.liftweb.json._
import scalax.io.Resource
import scalax.io.Codec
import net.liftweb.common.{Empty, Full, Failure, Loggable}

object Sender extends Loggable {
  implicit val jsonFormat = DefaultFormats
  implicit val codec = Codec("ASCII")

  def sendNow() {
    for (zabbixConf <- Config.zabbix) {
      val sendResult = tryo { send(zabbixConf) }
      sendResult match {
        case Failure(msg, _, _) => logger.warn("Failed: " + msg)
        case Full(response) => logger.info(response.response + ": " + response.info)
        case Empty => logger.error("Something unexpected happened")
      }
    }
  }

  def send(zabbixConfig: ZabbixConfig) = {
    def data: List[Data] =
      for {
        mbean <- MBeans.all
        property <- mbean.properties
        value <- tryo { mbean(property.propertyName) }
      } yield {
        Data(zabbixConfig.hostname, property.zabbixName, value.toString)
      }

    val toSend = ZabbixSenderData(data = data)

    logger.debug("data to send = " + toSend.data.map(d => d.key + " -> " + d.value).mkString("\n"))

    val socket = new Socket(zabbixConfig.server, zabbixConfig.serverPort)

    // NB: must use compact json rendering here (i.e. no whitespace) - zabbix
    // fails silently when encountering whitespace in json :(
    val m = ZabbixMessage(compact(render(Extraction.decompose(toSend))))

    logger.debug("writing to zabbix")
    socket.getOutputStream.write(m.asBytes.toArray)

    logger.debug("reading response")
    val input = Resource.fromInputStream(socket.getInputStream)
    val binaryResult = input.byteArray.toList

    logger.debug("binary result: " + binaryResult)
    val result = ZabbixMessage.parse(binaryResult)

    Serialization.read[ZabbixSenderDataResponse](result)
  }

}

