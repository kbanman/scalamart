package com.indilago.scalamart.util

import scala.concurrent.{ExecutionContext, Future}

trait Crud[Model,Identifier] {

  def find(entityId: Identifier)(implicit ec: ExecutionContext): Future[Option[Model]]

  /**
    * Create a new entity, returning the created record
    */
  def create(entity: Model)(implicit ec: ExecutionContext): Future[Model]

  def update(entity: Model)(implicit ec: ExecutionContext): Future[Model]

  /**
    * Delete an entity, returning number of affected records
    */
  def delete(entity: Model)(implicit ec: ExecutionContext): Future[Int]
}
