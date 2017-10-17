package com.indilago.scalamart.product.price

import java.time.{Clock, Instant}

import scala.concurrent.{ExecutionContext, Future}

class FakeProductPriceDao(override protected val clock: Clock) extends ProductPriceDao {

  @volatile
  private var records = Seq[ProductPrice]()

  override def prices(productId: Long)(implicit ec: ExecutionContext) = Future {
    records.filter(_.productId == productId)
      .sortBy(_.cardinality)
  }

  override def create(productPrice: ProductPrice)(implicit ec: ExecutionContext) = Future {
    val record = productPrice.copy(id = records.length + 1, created = Instant.now(clock))
    records = records :+ record
    record
  }

  def delete(priceId: Long)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == priceId)
    records = records.filterNot(_.id == priceId)
    affected
  }
}
