package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.option.ProductOptionType._
import com.indilago.scalamart.testutil.RandomHelpers

trait OptionHelpers { this: RandomHelpers =>

  def makeOption(kind: ProductOptionType) =
    ProductOption(
      id = 0,
      name = s"option_${alphaNum(3)}",
      kind = kind,
      defaultItemId = None
    )

  def makeOptionInput(o: ProductOption) =
    ProductOptionInput(o.name, o.kind, o.defaultItemId)

  def makeBasicItem(o: ProductOption) =
    BasicOptionItem(
      id = 0,
      optionId = o.id,
      name = s"item_${alphaNum(3)}"
    )

  def makeProductItem(o: ProductOption) =
    ProductOptionItem(
      id = 0,
      optionId = o.id,
      productId = positiveLong
    )

  def makeItem(o: ProductOption): OptionItem = o.kind match {
    case Basic => makeBasicItem(o)
    case Product => makeProductItem(o)
  }

  def makeItemInput: OptionItem => OptionItemInput = {
    case BasicOptionItem(id, optionId, name) =>
      BasicOptionItemInput(optionId, name)
    case ProductOptionItem(id, optionId, productId) =>
      ProductOptionItemInput(optionId, productId)
  }

  implicit class OptionHelpers(p: ProductOption) {
    def withoutId: ProductOption =
      p.copy(id = 0)
  }

  implicit class OptionItemHelpers(i: OptionItem) {
    def withoutId: OptionItem = i match {
      // @todo: There has to be a better way to do this
      case _i: BasicOptionItem => _i.copy(id = 0)
      case _i: ProductOptionItem => _i.copy(id = 0)
    }
  }
}
