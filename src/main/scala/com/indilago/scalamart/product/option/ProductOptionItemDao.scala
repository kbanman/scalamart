package com.indilago.scalamart.product.option

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.util.Crud

import scala.concurrent.{ExecutionContext, Future}

case class OptionItemRecord(
  id: Long,
  optionId: Long,
  name: Option[String],
  productId: Option[Long]
) extends Identifiable

trait ProductOptionItemDao extends Crud[OptionItemRecord, Long] {
  def itemsForOption(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItemRecord]]
}
