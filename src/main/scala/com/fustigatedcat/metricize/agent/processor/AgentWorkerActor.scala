package com.fustigatedcat.metricize.agent.processor

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.ActorSystems
import com.fustigatedcat.metricize.agent.intf.AgentWorkerInterface

object ProcessMessage {}

class AgentWorkerActor(worker : AgentWorkerInterface) extends Actor {

  def receive = {
    case ProcessMessage => {
      ActorSystems.statisticsProcessors ! worker.process()
    }
  }

}
