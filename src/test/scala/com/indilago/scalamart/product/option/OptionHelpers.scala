package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.BaseProduct
import com.indilago.scalamart.product.option.ProductOptionType._
import com.indilago.scalamart.testutil.RandomHelpers

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait OptionHelpers { this: RandomHelpers =>

  def productOptionDao: FakeProductOptionDao
  def productOptionItemDao: FakeProductOptionItemDao

  def makeOption(kind: ProductOptionType) =
    ProductOption(
      id = 0,
      name = s"option_${alphaNum(3)}",
      kind = kind,
      defaultItemId = None
    )

  def makeOptionInput(o: ProductOption) =
    ProductOptionInput(o.name, o.kind, o.defaultItemId)

  private def insertOptionF(product: BaseProduct, min: Int, max: Int): Future[ProductOption] =
    for {
      option <- productOptionDao.create(makeOption(ProductOptionType.Basic))
      _ <- productOptionDao.addOptionProduct(OptionProduct(option.id, product.id, min, max))
    } yield option

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

  implicit def item2Record: OptionItem => OptionItemRecord = {
    case BasicOptionItem(id, optionId, name) =>
      OptionItemRecord(id, optionId, Some(name), None)
    case ProductOptionItem(id, optionId, productId) =>
      OptionItemRecord(id, optionId, None, Some(productId))
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
