package com.indilago.scalamart.product.configuration

import java.time.Clock

import scala.concurrent.{ExecutionContext, Future}

class FakeProductConfigurationDao(protected val clock: Clock) extends ProductConfigurationDao {

  @volatile
  private var records = Seq[ProductConfig]()

  override def create(config: ProductConfig)(implicit ec: ExecutionContext) = Future {
    val record = config.copy(id = records.length + 1)
    records = records :+ record
    record
  }

  override def delete(config: ProductConfig)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == config.id)
    records = records.filterNot(_.id == config.id)
    affected
  }

  override def find(id: Long)(implicit ec: ExecutionContext) = Future {
    records.find(_.id == id)
  }

  override def update(config: ProductConfig)(implicit ec: ExecutionContext) = Future {
    val existing = records.find(_.id == config.id)
      .getOrElse(throw new RuntimeException("Cannot update a nonexistent product config"))
    records = records.updated(records.indexOf(existing), config)
    config
  }
}
