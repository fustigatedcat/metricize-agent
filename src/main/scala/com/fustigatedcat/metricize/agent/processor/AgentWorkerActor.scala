package com.fustigatedcat.metricize.agent.processor

import java.lang.reflect.Method

import akka.actor.Actor
import com.fustigatedcat.metricize.agent.intf.AgentWorkerInterface

object ProcessMessage {}

class AgentWorkerActor(worker : AgentWorkerInterface) extends Actor {

  def receive = {
    case ProcessMessage => {
      try {
        worker.process()
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

}
