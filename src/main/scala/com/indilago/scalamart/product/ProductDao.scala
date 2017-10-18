package com.indilago.scalamart.product

import scala.concurrent.{ExecutionContext, Future}

trait ProductDao {
  def search(id: Long)(implicit ec: ExecutionContext): Future[Option[BaseProduct]]
  def update(product: BaseProduct)(implicit ec: ExecutionContext): Future[BaseProduct]
  def delete(id: Long)(implicit ec: ExecutionContext): Future[Int]
  def create(product: BaseProduct)(implicit ec: ExecutionContext): Future[BaseProduct]
}
