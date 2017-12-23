package com.indilago.scalamart.util

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.services.ActionNotificationService
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.services.ActionType.ActionType

import scala.concurrent.{ExecutionContext, Future}

trait EventBusHelpers {

  protected val Create = ActionType.Create
  protected val Update = ActionType.Update
  protected val Delete = ActionType.Delete

  protected def notifier: ActionNotificationService

  protected def notify[T <: Identifiable](action: ActionType, entity: T, shouldNotify: Boolean = true)(implicit ec: ExecutionContext): Future[_] =
    if (shouldNotify)
      notifier.recordAction[T](action, entity)
    else
      Future.successful({})
}
