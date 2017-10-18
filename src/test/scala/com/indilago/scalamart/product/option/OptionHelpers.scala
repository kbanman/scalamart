package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.option.ProductOptionType._
import com.indilago.scalamart.testutil.RandomHelpers

trait OptionHelpers { this: RandomHelpers =>

  def makeOption(kind: ProductOptionType) = ProductOption(
    id = 0,
    name = s"option_${alphaNum(3)}",
    kind = kind,
    defaultItemId = None
  )

  def makeOptionInput(o: ProductOption): ProductOptionInput =
    ProductOptionInput(o.name, o.kind, o.defaultItemId)

  implicit class OptionHelpers(p: ProductOption) {
    def withoutId: ProductOption =
      p.copy(id = 0)
  }
}
