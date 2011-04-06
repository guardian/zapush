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

import sbt._

class ZabbixPushProject(info: ProjectInfo) extends DefaultWebProject(info) with Logback {
  //val guardianNexus  = "Guardian Nexus" at "http://nexus.gudev.gnl:8081/nexus/content/groups/public"
  val scalaToolsSnapshots = ScalaToolsSnapshots

  override def ivyUpdateLogging = UpdateLogging.Full

  val JETTY_VERSION = "7.0.1.v20091125"
  val SCALATEST = "1.3"
  val LIFT_VERSION = "2.3-SNAPSHOT"

  val iocore = "com.github.scala-incubator.io" %% "core" % "0.1.1" withSources()
  val iofile = "com.github.scala-incubator.io" %% "file" % "0.1.1" withSources()

  val joda = "joda-time" % "joda-time" % "1.6.2" withSources()

  val liftActor = "net.liftweb" %% "lift-actor" % LIFT_VERSION withSources()
  val liftCommon = "net.liftweb" %% "lift-common" % LIFT_VERSION withSources()
  val liftJson = "net.liftweb" %% "lift-json" % LIFT_VERSION withSources()
  val liftUtil = "net.liftweb" %% "lift-util" % LIFT_VERSION withSources()
  val liftWebkit = "net.liftweb" %% "lift-webkit" % LIFT_VERSION withSources()

  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided" withSources()

  val scalatest = "org.scalatest" % "scalatest" % SCALATEST % "test" withSources()
  val mockito = "org.mockito" % "mockito-all" % "1.8.0" % "test"

  // this is needed for jetty-run to work
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % JETTY_VERSION % "test"

}



trait Logback extends BasicScalaProject {
  val SLF4J_VERSION = "1.6.1"

  val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % SLF4J_VERSION withSources()
  val slf4jApi = "org.slf4j" % "slf4j-api" % SLF4J_VERSION withSources()
  val logback = "ch.qos.logback" % "logback-classic" % "0.9.27" withSources()

  // this stops us brining in any copies of log4j -
  //  becuase we replace with log4jOverSlf4j that forwards onto slf4j
  override def ivyXML =
    <dependencies>
       <exclude module="log4j"/>
    </dependencies>
}


 
