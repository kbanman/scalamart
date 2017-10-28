package com.indilago.scalamart.product.configuration

import com.indilago.scalamart.testutil.RandomHelpers

trait ProductConfigHelpers {
  this: RandomHelpers =>

  def makeConfig(productId: Long, options: Map[Long, Seq[(Long, Int)]] = Map.empty) =
    ProductConfig(
      id = 0,
      productId = productId,
      options = options
    )

  implicit class ProductConfigHelpers(p: ProductConfig) {
    def withoutId: ProductConfig =
      p.copy(id = 0)
  }
}
