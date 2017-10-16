package com.indilago.scalamart.dao

import java.time.{Clock, Instant}

import com.indilago.scalamart.models.product.ProductPrice

import scala.concurrent.{ExecutionContext, Future}

class FakeProductPriceDao(override val clock: Clock) extends ProductPriceDao {

  @volatile
  private var records = Seq[ProductPrice]()

  override def prices(productId: Long)(implicit ec: ExecutionContext) = Future {
    records.filter(_.productId == productId)
      .sortBy(_.cardinality)
  }

  override def createPrice(productPrice: ProductPrice)(implicit ec: ExecutionContext) = Future {
    val record = productPrice.copy(id = records.length + 1, created = Instant.now(clock))
    records = records :+ record
    record
  }

  def deletePrice(priceId: Long)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == priceId)
    records = records.filterNot(_.id == priceId)
    affected
  }
}
