package com.fustigatedcat.metricize.agent.processor

import java.io.{FileOutputStream, File, FileInputStream, FileWriter}
import java.net.URL
import java.nio.channels.Channels

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.{Agent, Configuration}
import dispatch._, Defaults._
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.JsonDSL._
import org.json4s.native._
import org.slf4j.LoggerFactory

import scala.io.Source

object LoadAgent {}

class AgentTypeLoaderActor extends Actor {

  val logger = LoggerFactory.getLogger(classOf[AgentTypeLoaderActor])

  implicit val formats = DefaultFormats

  def getAgentConfig : Option[JValue] = {
    val svc = Http(url(Configuration.api.baseurl + "/agents/me") <:< Map("Authorization" -> Agent.agentKey) OK as.String).either
    svc() match {
      case Left(err) => {
        logger.error("Failed to load agent config", err)
        None
      }
      case Right(str) => parseJsonOpt(str)
    }
  }

  def changed_?(filename : String, curr : JValue) : Boolean = {
    if (!new File(filename).exists()) {
      true
    } else {
      !(Source.fromInputStream(new FileInputStream(filename)).mkString == prettyJson(renderJValue(curr)))
    }
  }

  def receive = {
    case LoadAgent => getAgentConfig match {
      case Some(json) => (json \ "agentType").extractOpt[String] match {
        case Some(agentType) => {
          val newConfig = (json \ "config").merge("agentType" -> agentType : JObject)
          if(changed_?(Configuration.agent.conf.file, newConfig)) {
            logger.warn("Configuration has changed!")
            val file = new FileWriter(Configuration.agent.conf.file)
            file.write(prettyJson(renderJValue(newConfig)))
            file.close()
            logger.info("Restarting so that our agent picks up the new configuration")
            System.exit(1)
          } else {
            logger.debug("Configuration did not change")
          }
        }
        case _ =>
      }
      case _ =>
    }
  }

}
