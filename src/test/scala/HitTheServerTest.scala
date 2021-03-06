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
import zapush.{ZabbixSenderDataResponse, Data, ZabbixSenderData, ZabbixMessage}

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

    val data = ZabbixSenderData(data = List(Data("gnm40836", "java.test.blahx", "17")))

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
