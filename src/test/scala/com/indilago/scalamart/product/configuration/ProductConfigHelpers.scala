package com.indilago.scalamart.product.configuration

import com.indilago.scalamart.product.BaseProduct
import com.indilago.scalamart.testutil.RandomHelpers
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global

trait ProductConfigHelpers {
  this: RandomHelpers with ScalaFutures =>

  def productConfigDao: FakeProductConfigurationDao

  def makeConfig(productId: Long, options: Map[Long, Seq[(Long, Int)]] = Map.empty) =
    ProductConfig(
      id = 0,
      productId = productId,
      options = options,
      attributes = Map.empty
    )

  def makeConfig: ProductConfig =
    makeConfig(positiveLong)

  def makeConfig(product: BaseProduct): ProductConfig =
    makeConfig(product.id)

  def makeConfig(product: BaseProduct, options: Map[Long, Seq[(Long, Int)]]): ProductConfig =
    ProductConfig(id = 0, product.id, options, Map.empty)

  implicit class ProductConfigHelpers(p: ProductConfig) {
    def withoutId: ProductConfig =
      p.copy(id = 0)
    def insert(): ProductConfig =
      productConfigDao.create(p).futureValue
  }
}
