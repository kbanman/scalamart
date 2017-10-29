package com.indilago.scalamart.product.option.price

import java.time.Clock

import com.indilago.scalamart.testutil.FakeCrud

import scala.concurrent.{ExecutionContext, Future}

class FakeOptionPriceDao(override val clock: Clock) extends OptionPriceDao with FakeCrud[OptionPrice] {

  override protected def withId(entity: OptionPrice, id: Long): OptionPrice =
    entity.copy(id = id)

  override def prices(itemId: Long)(implicit ec: ExecutionContext) = Future {
    records.filter(_.itemId == itemId)
      .sortBy(_.cardinality)
  }
}
