package com.indilago.scalamart.product.price

import java.time.Instant
import java.util.Currency

import com.indilago.scalamart.testutil.{RandomHelpers, TestClock}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global

trait PriceHelpers { this: RandomHelpers with TestClock with ScalaFutures =>

  def productPriceDao: FakeProductPriceDao

  def makePrice(productId: Long = positiveLong, cardinality: Int = 0) = ProductPrice(
    id = 0,
    created = Instant.now(clock),
    productId = productId,
    currency = Currency.getInstance("USD"),
    amount = positiveDecimal,
    cardinality = cardinality,
    start = None,
    end = None
  )

  def makePrice(price: ProductPrice): ProductPrice =
    productPriceDao.create(price).futureValue

  def makePriceInput(price: ProductPrice) = ProductPriceInput(
    productId = price.productId,
    currency = price.currency,
    amount = price.amount,
    cardinality = Some(price.cardinality),
    start = price.start,
    end = price.end
  )

  implicit class ProductPriceHelpers(p: ProductPrice) {
    def withoutId: ProductPrice =
      p.copy(id = 0)
    def insert(): ProductPrice =
      productPriceDao.create(p).futureValue
  }
}
