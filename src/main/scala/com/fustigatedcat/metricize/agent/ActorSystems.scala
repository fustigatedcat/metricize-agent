package com.fustigatedcat.metricize.agent

import akka.actor.{Props, ActorSystem}
import akka.routing.RoundRobinPool
import com.fustigatedcat.metricize.agent.intf.AgentWorkerInterface
import com.fustigatedcat.metricize.agent.processor.{ProcessMessage, AgentWorkerActor, StatisticsProcessorActor}
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

object ActorSystems {

  val generalActorSystem = ActorSystem("generalActorSystem")

  val statisticsProcessors = generalActorSystem.actorOf(RoundRobinPool(nrOfInstances = 5).props(Props[StatisticsProcessorActor]))

  val workerProcessor = Configuration.agentConf match {
    case Some(config) if Symbol(config.getString("agentType")) != 'NONE => {
        val clazz = Class.forName(s"com.fustigatedcat.metricize.agent.${config.getString("agentType")}AgentWorker")
        val const = clazz.getDeclaredConstructor(classOf[Config])
        Some(ActorSystems.generalActorSystem.actorOf(Props(classOf[AgentWorkerActor], const.newInstance(config).asInstanceOf[AgentWorkerInterface])))
    }
    case _ => None
  }

  def scheduleWorkerProcessor(dur : FiniteDuration) = {
    import generalActorSystem.dispatcher
    generalActorSystem.scheduler.scheduleOnce(dur, workerProcessor.get, ProcessMessage)
  }

}
