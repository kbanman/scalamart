package com.indilago.scalamart.product

import java.time.Instant

import com.indilago.scalamart.testutil.{RandomHelpers, TestClock}

trait ProductHelpers {
  this: RandomHelpers with TestClock =>

  def makeProduct(availabilityStart: Option[Instant], availabilityEnd: Option[Instant]) =
    BasicProduct(
      id = 0,
      created = Instant.now(clock),
      name = s"Product ${alphaNum(6)}",
      description = s"Description ${words(10)}",
      availabilityStart = availabilityStart,
      availabilityEnd = availabilityEnd
    )

  def makeProduct: BasicProduct =
    makeProduct(None, None)

  def makeProductInput(product: BasicProduct): ProductInput =
    ProductInput(
      name = product.name,
      description = product.description,
      availabilityStart = product.availabilityStart,
      availabilityEnd = product.availabilityEnd
    )

  implicit class ProductHelpers(p: BasicProduct) {
    def withoutId: BasicProduct =
      p.copy(id = 0)
  }
}
