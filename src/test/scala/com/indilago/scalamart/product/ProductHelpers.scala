package com.indilago.scalamart.product

import java.time.Instant

import com.indilago.scalamart.testutil.{RandomHelpers, TestClock}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.ExecutionContext.Implicits.global

trait ProductHelpers {
  this: RandomHelpers with TestClock with ScalaFutures =>

  def productDao: FakeProductDao

  def makeProduct(availabilityStart: Option[Instant], availabilityEnd: Option[Instant]) =
    BaseProduct(
      id = 0,
      created = Instant.now(clock),
      name = s"Product ${alphaNum(6)}",
      description = s"Description ${words(10)}",
      availabilityStart = availabilityStart,
      availabilityEnd = availabilityEnd
    )

  def makeProduct: BaseProduct =
    makeProduct(None, None)

  def makeProductInput(product: BaseProduct): ProductInput =
    ProductInput(
      name = product.name,
      description = product.description,
      availabilityStart = product.availabilityStart,
      availabilityEnd = product.availabilityEnd
    )

  implicit class ProductHelpers(p: BaseProduct) {
    def withoutId: BaseProduct =
      p.copy(id = 0)
    def insert(): BaseProduct =
      productDao.create(p).futureValue
  }
}
