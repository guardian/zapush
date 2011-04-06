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

