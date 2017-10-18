package com.indilago.scalamart.product.option

import javax.inject.Singleton

import com.indilago.scalamart.product.BaseProduct

import scala.concurrent.{ExecutionContext, Future}

class FakeProductOptionDao extends ProductOptionDao {

  @volatile
  private var records = Seq[ProductOption]()

  @volatile
  private var items = Seq[OptionItemRecord]()

  @volatile
  private var products = Seq[OptionProduct]()

  def optionsForProduct(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]] = Future {
    products.filter(_.productId == product.id)
      .map(_.optionId)
      .map(find)
  }

  def addOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int] = Future {
    products.find(_ == op) match {
      case Some(_) => 0
      case None =>
        products = products :+ op
        1
    }
  }

  def removeOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int] = Future {
    products.find(_ == op) match {
      case Some(_) =>
        products = products.filterNot(_ == op)
        1
      case None => 0
    }
  }

  def search(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]] = Future {
    records.find(_.id == optionId)
  }

  def create(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption] = Future {
    val created = option.copy(id = records.length+1)
    records = records :+ created
    created
  }

  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption] = Future {
    val existing = find(option.id)
    records = records.updated(records.indexOf(existing), option)
    option
  }

  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Int] = Future {
    records.find(_.id == option.id) match {
      case Some(_) =>
        records = records.filterNot(_.id == option.id)
        1
      case None => 0
    }
  }

  def itemsForOption(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItemRecord]] = Future {
    items.filter(_.optionId == option.id)
  }

  def createItem(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItemRecord] = Future {
    val record = toRecord(item).copy(id = items.length+1)
    items = items :+ record
    record
  }

  def updateItem(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItemRecord] = Future {
    val existing = findItem(item.id)
    val record = toRecord(item)
    items = items.updated(items.indexOf(existing), record)
    record
  }

  def deleteItem(item: OptionItem)(implicit ec: ExecutionContext): Future[Int] = Future {
    items.find(_.id == item.id) match {
      case Some(_) =>
        items = items.filterNot(_.id == item.id)
        1
      case None => 0
    }
  }

  private def find(optionId: Long): ProductOption =
    records.find(_.id == optionId).get

  private def findItem(itemId: Long): OptionItemRecord =
    items.find(_.id == itemId).get

  private def toRecord: OptionItem => OptionItemRecord = {
    case BasicOptionItem(id, optionId, name) =>
      OptionItemRecord(id, optionId, Some(name), None)
    case ProductOptionItem(id, optionId, productId) =>
      OptionItemRecord(id, optionId, None, Some(productId))
  }
}
