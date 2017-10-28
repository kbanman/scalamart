package com.indilago.scalamart.product.configuration

import scala.concurrent.{ExecutionContext, Future}

trait ProductConfigurationDao {
  def find(configId: Long)(implicit ec: ExecutionContext): Future[Option[ProductConfig]]

  def create(config: ProductConfig)(implicit ec: ExecutionContext): Future[ProductConfig]

  def update(config: ProductConfig)(implicit ec: ExecutionContext): Future[ProductConfig]

  def delete(config: ProductConfig)(implicit ec: ExecutionContext): Future[Int]
}
