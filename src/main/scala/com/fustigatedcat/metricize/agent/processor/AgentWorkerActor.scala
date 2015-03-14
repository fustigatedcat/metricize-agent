package com.fustigatedcat.metricize.agent.processor

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.intf.{AgentResponseFailure, AgentResponseSuccess, AgentWorkerInterface}

object ProcessMessage {}

class AgentWorkerActor(worker : AgentWorkerInterface) extends Actor {

  def receive = {
    case ProcessMessage => {
      worker.process() match {
        case AgentResponseSuccess(time, message) => println("Success took " + time + "ms: " + message)
        case AgentResponseFailure(time, message) => println("Failure took " + time + "ms: " + message)
      }
    }
  }

}
