package com.indilago.scalamart.product

import java.time.{Clock, Instant}

import scala.concurrent.{ExecutionContext, Future}

class FakeProductDao(protected val clock: Clock) extends ProductDao {

  @volatile
  private var records = Seq[BasicProduct]()

  override def create(product: BasicProduct)(implicit ec: ExecutionContext) = Future {
    val record = product.copy(id = records.length + 1, created = Instant.now(clock))
    records = records :+ record
    record
  }

  override def delete(id: Long)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == id)
    records = records.filterNot(_.id == id)
    affected
  }

  override def search(id: Long)(implicit ec: ExecutionContext) = Future {
    records.find(_.id == id)
  }

  override def update(product: BasicProduct)(implicit ec: ExecutionContext) = Future {
    val existing = records.find(_.id == product.id)
      .getOrElse(throw new RuntimeException("Cannot update a nonexistent product"))
    records.updated(records.indexOf(existing), product)
    product
  }
}
