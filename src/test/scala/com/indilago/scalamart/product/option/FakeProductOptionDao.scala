package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.BaseProduct
import com.indilago.scalamart.testutil.FakeCrud

import scala.concurrent.{ExecutionContext, Future}

class FakeProductOptionDao extends ProductOptionDao with FakeCrud[ProductOption] {

  override protected def withId(option: ProductOption, id: Long): ProductOption =
    option.copy(id = id)

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
}
