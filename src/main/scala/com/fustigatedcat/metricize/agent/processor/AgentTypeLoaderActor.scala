package com.fustigatedcat.metricize.agent.processor

import java.io.FileWriter

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.{Agent, Configuration}
import dispatch._, Defaults._
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.native._
import org.slf4j.LoggerFactory

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

  def receive = {
    case LoadAgent => getAgentConfig match {
      case Some(json) => (json \ "agentType").extractOpt[String] match {
        case Some(agentType) if Symbol(agentType) != 'NONE => {
          val file = new FileWriter(Configuration.agent.conf.file)
          file.write(prettyJson(renderJValue(json \ "config")))
          file.close()
          if (Configuration.agentConf.isEmpty) {
            logger.info("Got configuration, restarting so that our agent starts up")
            System.exit(1)
          }
        }
        case Some(agentType) => logger.warn("Agent type not set yet")
        case _ =>
      }
      case _ =>
    }
  }

}
