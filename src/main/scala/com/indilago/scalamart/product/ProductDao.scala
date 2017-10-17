package com.indilago.scalamart.product

import scala.concurrent.{ExecutionContext, Future}

trait ProductDao {
  def search(id: Long)(implicit ec: ExecutionContext): Future[Option[BasicProduct]]
  def update(product: BasicProduct)(implicit ec: ExecutionContext): Future[BasicProduct]
  def delete(id: Long)(implicit ec: ExecutionContext): Future[Int]
  def create(product: BasicProduct)(implicit ec: ExecutionContext): Future[BasicProduct]
}
