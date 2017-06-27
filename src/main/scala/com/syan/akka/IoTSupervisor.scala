package com.syan.akka

import akka.actor.{ Actor, ActorLogging, Props }

object IoTSupervisor {
  def props(): Props = Props(new IoTSupervisor)
}

class IoTSupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("IoT Application started")
  override def postStop(): Unit = log.info("IoT Application stopped")

  override def receive: Receive = Actor.emptyBehavior
}
