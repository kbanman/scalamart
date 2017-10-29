package com.indilago.scalamart.product.price

import java.time.{Clock, Instant}

import com.indilago.scalamart.testutil.FakeCrud

import scala.concurrent.{ExecutionContext, Future}

class FakeProductPriceDao(override protected val clock: Clock) extends ProductPriceDao with FakeCrud[ProductPrice] {

  override protected def withId(price: ProductPrice, id: Long): ProductPrice =
    price.copy(id = id)

  override protected def prepareForInsert(price: ProductPrice): ProductPrice =
    price.copy(created = Instant.now(clock))

  override def prices(productId: Long)(implicit ec: ExecutionContext) = Future {
    records.filter(_.productId == productId)
      .sortBy(_.cardinality)
  }
}
