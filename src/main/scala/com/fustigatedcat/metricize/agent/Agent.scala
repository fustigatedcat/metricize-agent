package com.fustigatedcat.metricize.agent

import dispatch._, Defaults._
import org.json4s.JsonAST.{JValue, JObject}
import org.json4s.native.parseJsonOpt

object Agent {

  def getCustomer() : JValue = {
    val svc = Http(url(Configuration.api.baseurl + "/customers/me") <:< Map("Authorization" -> "ABCD1234") OK as.String)
    parseJsonOpt(svc()) match {
      case Some(json) => json
      case _ => JObject()
    }
  }

  def getAgentConfig() : JValue = {
    val svc = Http(url(Configuration.api.baseurl + "/customers/me") <:< Map() OK as.String)
    parseJsonOpt(svc()) match {
      case Some(json) => json
      case _ => JObject()
    }
  }

  def main(args : Array[String]) : Unit = {
    println(getCustomer())
  }

}