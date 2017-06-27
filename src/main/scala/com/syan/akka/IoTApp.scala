package com.syan.akka

import akka.actor.ActorSystem

import scala.io.StdIn

object IoTApp {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("iot-system")

    try {
      // Create top level supervisor
      val supervisor = system.actorOf(IoTSupervisor.props(), "iot-supervisor")

      println(supervisor)

      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
