package com.syan.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.syan.akka.DeviceGroup.{ReplyDeviceList, RequestDeviceList}

object DeviceGroup {
  def props(groupId: String): Props = Props(new DeviceGroup(groupId))

  final case class RequestDeviceList(requestId: Long)
  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

  final case class RequestAllTemperatures(requestId: Long)
  final case class RespondAllTemperatures(requestId: Long, temperatures: Map[String, TemperatureReading])

  sealed trait TemperatureReading
  final case class Temperature(value: Double) extends TemperatureReading
  case object TemperatureNotAvailable extends TemperatureReading
  case object DeviceNotAvailable extends TemperatureReading
  case object DeviceTimedOut extends TemperatureReading
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  var deviceIdToActor = Map.empty[String, ActorRef]
  var actorToDeviceId = Map.empty[ActorRef, String]

  override def preStart(): Unit = log.info(s"DeviceGroup $groupId started")

  override def postStop(): Unit = log.info(s"DeviceGroup $groupId stopped")

  override def receive: Receive ={
    case trackMsg @ RequestTrackDevice(`groupId`, _) =>
      deviceIdToActor.get(trackMsg.deviceId) match {
        case Some(deviceActor) =>
          deviceActor forward trackMsg

        case None =>
          log.info(s"Creating device actor for ${trackMsg.deviceId}")
          val deviceActor = context.actorOf(Device.props(groupId, trackMsg.deviceId))
          context.watch(deviceActor)
          deviceIdToActor += trackMsg.deviceId -> deviceActor
          actorToDeviceId += deviceActor -> trackMsg.deviceId
          deviceActor forward trackMsg
      }

    case RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        s"Ignoring TrackDevice request for $groupId. This actor is responsible for ${this.groupId}.")

    case RequestDeviceList(requestId) =>
      sender() ! ReplyDeviceList(requestId, deviceIdToActor.keySet)

    case Terminated(deviceActor) =>
      val deviceId = actorToDeviceId(deviceActor)
      log.info(s"Device actor for $deviceId has been terminated")
      actorToDeviceId -= deviceActor
      deviceIdToActor -= deviceId

  }
}
