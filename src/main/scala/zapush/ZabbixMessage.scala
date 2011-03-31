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















