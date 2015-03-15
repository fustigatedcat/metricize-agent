package com.fustigatedcat.metricize.agent

import akka.actor.{Props, ActorSystem}
import akka.routing.RoundRobinPool
import com.fustigatedcat.metricize.agent.processor.StatisticsProcessorActor

object ActorSystems {

  val generalActorSystem = ActorSystem("generalActorSystem")

  val statisticsProcessors = generalActorSystem.actorOf(RoundRobinPool(nrOfInstances = 5).props(Props[StatisticsProcessorActor]))

}
