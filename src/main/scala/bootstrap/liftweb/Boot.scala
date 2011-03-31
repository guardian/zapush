package bootstrap.liftweb

import net.liftweb.http._


class Boot {
  def boot() {
    println("hello!")
    LiftRules.htmlProperties.default.set((r: Req) =>new Html5Properties(r.userAgent))

    LiftRules.addToPackages("zapush")
  }
}