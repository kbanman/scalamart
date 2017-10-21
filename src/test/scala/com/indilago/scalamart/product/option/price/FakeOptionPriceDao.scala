package com.indilago.scalamart.product.option.price

import java.time.Clock

import scala.concurrent.{ExecutionContext, Future}

class FakeOptionPriceDao(override val clock: Clock) extends OptionPriceDao {

  @volatile
  private var records = Seq[OptionPrice]()

  override def prices(itemId: Long)(implicit ec: ExecutionContext) = Future {
    records.filter(_.itemId == itemId)
      .sortBy(_.cardinality)
  }

  override def create(price: OptionPrice)(implicit ec: ExecutionContext) = Future {
    val record = price.copy(id = records.length + 1)
    records = records :+ record
    record
  }

  def delete(priceId: Long)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == priceId)
    records = records.filterNot(_.id == priceId)
    affected
  }
}
