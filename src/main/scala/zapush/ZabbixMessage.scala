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

import scalax.io.OutputConverter._

trait ZabbixJsonMessage
case class ZabbixSenderData(request: String = "sender data", data: List[Data]) extends ZabbixJsonMessage
case class Data(host: String, key: String, value: String)

case class ZabbixSenderDataResponse(response: String, info: String)


case class ZabbixMessage(message: String) {
  lazy val asBytes: List[Byte] = ZabbixMessage.header ::: size ::: payloadBytes
  lazy val payloadBytes = message.getBytes("ASCII").toList
  lazy val length = payloadBytes.size
  lazy val size = LongConverter.toBytes(length).toList.reverse
}

object ZabbixMessage {
  val header = List[Byte]('Z', 'B', 'X', 'D', 1)
  val sizeLen = LongConverter.sizeInBytes

  def parse(m: List[Byte]) =
    new String(m.drop(ZabbixMessage.header.length + ZabbixMessage.sizeLen).toArray, "ASCII")
}















