package com.indilago.scalamart.product

import java.time.{Clock, Instant}

import com.indilago.scalamart.testutil.FakeCrud

class FakeProductDao(protected val clock: Clock) extends ProductDao with FakeCrud[BaseProduct] {

  override def withId(product: BaseProduct, id: Long): BaseProduct =
    product.copy(id = id)

  override def prepareForInsert(product: BaseProduct): BaseProduct =
    product.copy(created = Instant.now(clock))
}
