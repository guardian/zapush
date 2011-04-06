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

package zapush.snippet

import net.liftweb.util.Helpers._
import xml.Text
import zapush.MBeans

class ShowMBeans {
  def render = "li" #> MBeans.all.flatMap(_.properties).sortBy(_.zabbixName).map { prop =>
      ".prop-name *" #> prop.zabbixName &
      ".prop-type" #> prop.attributeType &
      ".prop-value" #> tryo { Text(prop.value.toString) }.openOr(<strong>error!</strong>)
  }
}