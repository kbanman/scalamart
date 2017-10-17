package com.indilago.scalamart.services

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.services.ActionType.ActionType
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

object ActionType extends Enumeration {
  type ActionType = Value
  val Create = Value("Create")
  val Read = Value("Read")
  val Update = Value("Update")
  val Delete = Value("Delete")
}

@ImplementedBy(classOf[LoggingActionNotificationService])
trait ActionNotificationService {
  def recordAction(
    action: ActionType,
    subject: String,
    identifier: Any,
    comment: Option[String]
  )(implicit ec: ExecutionContext): Future[_]

  def recordAction(
    action: ActionType,
    subject: Class[_],
    identifier: Any,
    comment: Option[String] = None
  )(implicit ec: ExecutionContext): Future[_] =
    recordAction(action, subject.getSimpleName, identifier, comment)

  def recordAction[T <: Identifiable](action: ActionType, subject: T)(implicit ec: ExecutionContext): Future[_] =
    recordAction(action, subject.getClass, subject.id)
}

@Singleton
class LoggingActionNotificationService extends ActionNotificationService with StrictLogging {
  override def recordAction(
    action: ActionType,
    subject: String,
    identifier: Any,
    comment: Option[String]
  )(implicit ec: ExecutionContext) = Future {
    logger.info(s"$action $subject $identifier${comment.map(c => s" - $c").getOrElse("")}")
  }
}
