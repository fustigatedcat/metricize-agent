package com.fustigatedcat.metricize.agent.processor

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import com.fustigatedcat.metricize.agent.{Agent, Configuration}
import dispatch._, Defaults._

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.intf.{AgentResponse, AgentResponseFailure, AgentResponseSuccess}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.native.{prettyJson, renderJValue}

class StatisticsProcessorActor extends Actor {

  def gzipMessage(str : String) : String = {
    val os = new ByteArrayOutputStream()
    val gzos = new GZIPOutputStream(os)
    gzos.write(str.getBytes)
    gzos.finish()
    Base64.encodeBase64String(os.toByteArray)
  }

  def convertMessage(response : AgentResponse) : JValue = response match {
    case AgentResponseSuccess(startTime, time, message) => {
      val encoded = gzipMessage(message)
      ("status" -> "success") ~
        ("startTime" -> startTime) ~
        ("time" -> time) ~
        ("md5" -> DigestUtils.md5Hex(message)) ~
        ("msg" -> encoded)
    }
    case AgentResponseFailure(startTime, time, message) => {
      val encoded = gzipMessage(message)
      ("status" -> "failure") ~
        ("startTime" -> startTime) ~
        ("time" -> time) ~
        ("md5" -> DigestUtils.md5Hex(message)) ~
        ("msg" -> encoded)
    }
  }

  def postMessage(message : JValue) = {
    val body = prettyJson(renderJValue(message))
    println(s"Sending body $body")
    val svc = Http(url(s"${Configuration.api.baseurl}/statistics").POST <:<
      Map("Content-Type" -> "application/json", "Authorization" -> Agent.agentKey) <<
      body
      OK as.String
    ).either
    svc() match {
      case Left(err) =>
      case Right(success) =>
    }
  }

  def receive = {
    case response : AgentResponse => postMessage(convertMessage(response))
  }

}
