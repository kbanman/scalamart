package com.indilago.scalamart.product.option

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.product.BaseProduct

import scala.concurrent.{ExecutionContext, Future}

case class OptionProduct(
  optionId: Long,
  productId: Long
) extends Identifiable {
  def id: (Long, Long) =
    optionId -> productId
}

trait ProductOptionDao {
  def optionsForProduct(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]]

  def addOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int]

  def removeOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int]

  def search(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]]

  def create(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption]

  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption]

  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Int]
}
