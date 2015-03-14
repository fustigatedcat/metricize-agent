package com.fustigatedcat.metricize.agent

import java.io.{FileReader, FileInputStream, FileWriter, File}
import java.util.concurrent.TimeUnit

import akka.actor.Props
import com.fustigatedcat.metricize.agent.processor.{LoadAgent, AgentTypeLoaderActor}
import com.typesafe.config.ConfigFactory
import dispatch._, Defaults._
import org.apache.commons.lang3.RandomStringUtils
import org.json4s.DefaultFormats
import org.json4s.native.{parseJsonOpt, renderJValue, prettyJson}
import org.json4s.JsonDSL._
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.io.Source

object Agent {

  val logger = LoggerFactory.getLogger(Agent.getClass)

  implicit val formats = DefaultFormats

  val customerKey = {
    val file = new File(Configuration.keyfile.customer)
    if(file == null || !file.exists()) {
      logger.error(s"Missing customer-key file [${Configuration.keyfile.customer}]")
      System.exit(-1)
    }
    Source.fromInputStream(new FileInputStream(file)).mkString
  }

  val agentKey = {
    val file = new File(Configuration.keyfile.agent)
    if(file == null || !file.exists()) {
      createAgent match {
        case Some(key) => {
          logger.debug(s"Just loaded key [$key]")
          key
        }
        case _ => {
          logger.error("Could not find agent-key file nor could we load it remotely")
          System.exit(-1)
        }
      }
    }
    Source.fromInputStream(new FileInputStream(file)).mkString
  }

  def createAgent: Option[String] = {
    val file = new FileWriter(Configuration.keyfile.agent)
    val svc = Http(
      url(Configuration.api.baseurl + "/agents").POST <:<
        Map("Content-Type" -> "application/json", "Authorization" -> Agent.customerKey) <<
        prettyJson(renderJValue("name" -> RandomStringUtils.randomAlphanumeric(64)))
        OK as.String
    ).either
    svc() match {
      case Left(err) => {
        logger.error("Failed to load configuration", err)
        System.exit(-1)
        None
      }
      case Right(rtn) => parseJsonOpt(rtn) match {
        case Some(json) => (json \ "agentKey").extractOpt[String] match {
          case Some(key) => {
            file.write(key)
            file.close()
            Some(key)
          }
          case _ => {
            logger.error("agentKey was not provided in the return")
            System.exit(-1)
            None
          }
        }
        case _ => {
          logger.error("Received invalid JSON when creating agent")
          System.exit(-1)
          None
        }
      }
    }
  }

  def startAgentTypeLoaderActor = {
    ActorSystems.generalActorSystem.scheduler.schedule(
      Duration.Zero,
      Duration.create(Configuration.agent.conf.reload, TimeUnit.SECONDS),
      ActorSystems.generalActorSystem.actorOf(Props[AgentTypeLoaderActor]),
      LoadAgent
    )
  }

  def main(args : Array[String]) : Unit = {
    startAgentTypeLoaderActor
  }

}