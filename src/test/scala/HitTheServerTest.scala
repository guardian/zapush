import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory.Default
import java.io.OutputStream
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, FunSuite}
import scalax.io.Codec
import java.net.Socket
import scalax.io.OutputConverter._
import scalax.io.Resource
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

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


class ZabbixMessageTest extends FlatSpec with ShouldMatchers {
  val msg = List[Byte]('Z', 'B', 'X', 'D', 1, 5, 0, 0, 0, 0, 0, 0, 0, 114, 114)

  "parser" should "correctly parse a well formatted message" in {

    val result = ZabbixMessage.parse(msg)
    result should be ("rr")
  }
}


class HitTheServerTest extends FunSuite {


  implicit val jsonFormat = DefaultFormats

  implicit val codec = Codec("ASCII")

  ignore("try hitting zabbix server to get list of stuff") {

    val socket = new Socket("gnm40833.int.gnl", 10051)

    val message = """{"request":"active checks","host":"gnm40836"}"""

    val m = ZabbixMessage(message)
    println(m.asBytes)

    println("writing")
    socket.getOutputStream.write(m.asBytes.toArray)

    println("reading...")
    val input = Resource.fromInputStream(socket.getInputStream)
    val result = input.byteArray
    println("I got result: " + result.toList)
  }


  test("sending a trap") {

    val data = ZabbixSenderData(data = List(Data("gnm40836", "java.test.blah", "17")))

    val socket = new Socket("gnm40833.int.gnl", 10051)

    val m = ZabbixMessage(compact(render(Extraction.decompose(data))))
    println("message is: " + m.message)
    println("or: " + m.asBytes)

    println("writing")
    socket.getOutputStream.write(m.asBytes.toArray)

    println("reading...")
    val input = Resource.fromInputStream(socket.getInputStream)
    val result = ZabbixMessage.parse(input.byteArray.toList)
    println("the result is: " + result)

    val caseClassResult = Serialization.read[ZabbixSenderDataResponse](result)
    println("and in english: " + caseClassResult)
  }

}
