package com.indilago.scalamart.services
import com.indilago.scalamart.services.ActionType.ActionType

import scala.concurrent.{ExecutionContext, Future}

class FakeNotificationService extends ActionNotificationService {

  case class Event(action: ActionType, subject: String, identifier: Any, comment: Option[String])

  @volatile
  private var events = Seq[Event]()

  def recordAction(action: ActionType, subject: String, identifier: Any, comment: Option[String])(implicit ec: ExecutionContext): Future[Unit] = {
    val event = Event(action, subject, identifier, comment)
    events = events :+ event
    Future.successful({})
  }

  def find(action: ActionType, subject: Class[_]): Seq[Event] =
    events.filter(e => e.action == action && e.subject == subject.getSimpleName)

  def reset(): Unit =
    events = Seq()
}
