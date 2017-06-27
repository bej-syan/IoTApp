package com.syan.akka

import akka.actor.{Actor, ActorLogging, Props}

object Device {
  def props(groupId: String, deviceId: String): Props =
    Props(new Device(groupId, deviceId))

  final case class RecordTemperature(requestId: Long, value: Double)
  final case class TemperatureRecorded(requestId: Long)

  final case class ReadTemperature(requestId: Long)
  final case class ResponseTemperature(requestId: Long, value: Option[Double])

  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {
  import Device._

  var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.info(s"Device actor {$groupId}-{$deviceId} started")

  override def postStop(): Unit = log.info(s"Device actor {$groupId}-{$deviceId} stopped")

  override def receive: Receive = {
    case RequestTrackDevice(`groupId`, `deviceId`) =>
      sender() ! DeviceRegistered

    case RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        s"Ignoring TrackDevice Request for $groupId-$deviceId. This actor is responsible for ${this.groupId}-${this.deviceId}"
      )

    case RecordTemperature(id, value) =>
      log.info(s"Recorded temperature reading $value with $id")
      lastTemperatureReading = Some(value)
      sender() ! TemperatureRecorded(id)

    case ReadTemperature(id) =>
      sender() ! ResponseTemperature(id, lastTemperatureReading)
  }
}
