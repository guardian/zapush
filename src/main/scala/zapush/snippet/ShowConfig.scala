package zapush.snippet

import net.liftweb.util.Helpers._
import zapush.Config


class ShowConfig {
  def render = "*" #> Config.toString
}