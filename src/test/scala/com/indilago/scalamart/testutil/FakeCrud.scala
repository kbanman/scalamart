package com.indilago.scalamart.testutil

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.util.Crud

import scala.concurrent.{ExecutionContext, Future}

trait FakeCrud[T <: Identifiable] extends Crud[T, Long] {
  @volatile
  protected var records: Seq[T] = Seq[T]()

  protected def withId(entity: T, id: Long): T

  protected def prepareForInsert(entity: T): T =
    entity

  override def create(entity: T)(implicit ec: ExecutionContext) = Future {
    val record = withId(entity, records.length + 1)
    records = records :+ prepareForInsert(record)
    record
  }

  override def delete(entity: T)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == entity.id)
    records = records.filterNot(_.id == entity.id)
    affected
  }

  override def find(id: Long)(implicit ec: ExecutionContext) = Future {
    records.find(_.id == id)
  }

  override def update(entity: T)(implicit ec: ExecutionContext) = Future {
    val existing = records.find(_.id == entity.id)
      .getOrElse(throw new RuntimeException("Cannot update a nonexistent record"))
    records = records.updated(records.indexOf(existing), entity)
    entity
  }

  protected def require(id: Long): T =
    records.find(_.id == id).get
}
