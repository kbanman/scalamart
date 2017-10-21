package com.indilago.scalamart.product.option

import scala.concurrent.{ExecutionContext, Future}

class FakeProductOptionItemDao extends ProductOptionItemDao {

  @volatile
  private var items = Seq[OptionItemRecord]()

  def itemsForOption(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItemRecord]] = Future {
    items.filter(_.optionId == option.id)
  }

  def search(itemId: Long)(implicit ec: ExecutionContext): Future[Option[OptionItemRecord]] = Future {
    items.find(_.id == itemId)
  }

  def create(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItemRecord] = Future {
    val record = toRecord(item).copy(id = items.length+1)
    items = items :+ record
    record
  }

  def update(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItemRecord] = Future {
    val existing = find(item.id)
    val record = toRecord(item)
    items = items.updated(items.indexOf(existing), record)
    record
  }

  def delete(item: OptionItem)(implicit ec: ExecutionContext): Future[Int] = Future {
    items.find(_.id == item.id) match {
      case Some(_) =>
        items = items.filterNot(_.id == item.id)
        1
      case None => 0
    }
  }

  private def find(itemId: Long): OptionItemRecord =
    items.find(_.id == itemId).get

  private def toRecord: OptionItem => OptionItemRecord = {
    case BasicOptionItem(id, optionId, name) =>
      OptionItemRecord(id, optionId, Some(name), None)
    case ProductOptionItem(id, optionId, productId) =>
      OptionItemRecord(id, optionId, None, Some(productId))
  }
}
