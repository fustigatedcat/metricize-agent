package com.fustigatedcat.metricize.agent.processor

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.{Configuration, ActorSystems}
import com.fustigatedcat.metricize.agent.intf.AgentWorkerInterface
import concurrent.duration._

object ProcessMessage {}

class AgentWorkerActor(worker : AgentWorkerInterface) extends Actor {

  lazy val finiteDuration = {
    val countPer = Configuration.agentConf.get.getInt("countPer")
    Configuration.agentConf.get.getString("timeUnit") match {
      case "SECOND" => countPer seconds
      case "MINUTE" => countPer minutes
      case "HOUR" => countPer hours
    }
  }

  def receive = {
    case ProcessMessage => {
      ActorSystems.statisticsProcessors ! worker.process()
      if(worker.needsRescheduling_?) {
        ActorSystems.scheduleWorkerProcessor(finiteDuration)
      }
    }
  }

}
