package com.indilago.scalamart.product.option

import com.indilago.scalamart.Identifiable

import scala.concurrent.{ExecutionContext, Future}

case class OptionItemRecord(
  id: Long,
  optionId: Long,
  name: Option[String],
  productId: Option[Long]
) extends Identifiable

trait ProductOptionItemDao {
  def itemsForOption(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItemRecord]]

  def search(itemId: Long)(implicit ec: ExecutionContext): Future[Option[OptionItemRecord]]

  def create(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItemRecord]

  def update(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItemRecord]

  def delete(item: OptionItem)(implicit ec: ExecutionContext): Future[Int]
}
