package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.BaseProduct

import scala.concurrent.{ExecutionContext, Future}

class FakeProductOptionDao extends ProductOptionDao {

  @volatile
  private var records = Seq[ProductOption]()

  @volatile
  private var products = Seq[OptionProduct]()

  def optionsForProduct(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]] = Future {
    products.filter(_.productId == product.id)
      .map(_.optionId)
      .map(require)
  }

  def findOptionProduct(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Option[OptionProduct]] = Future {
    products.find(op => op.productId == product.id && op.optionId == option.id)
  }

  def addOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int] = Future {
    products.find(_ == op) match {
      case Some(_) => 0
      case None =>
        products = products :+ op
        1
    }
  }

  def updateOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[OptionProduct] = Future {
    val existing = products.find(_.id == op.id).get
    products = products.updated(products.indexOf(existing), op)
    op
  }

  def removeOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int] = Future {
    products.find(_ == op) match {
      case Some(_) =>
        products = products.filterNot(_ == op)
        1
      case None => 0
    }
  }

  def find(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]] = Future {
    records.find(_.id == optionId)
  }

  def create(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption] = Future {
    val created = option.copy(id = records.length+1)
    records = records :+ created
    created
  }

  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption] = Future {
    val existing = require(option.id)
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

  private def require(optionId: Long): ProductOption =
    records.find(_.id == optionId).get
}
