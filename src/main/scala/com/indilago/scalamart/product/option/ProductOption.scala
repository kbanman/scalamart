package com.indilago.scalamart.product.option

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.product.option.ProductOptionType.{Basic, Product, ProductOptionType}

case class ProductOption(
  id: Long,
  name: String,
  kind: ProductOptionType,
  defaultItemId: Option[Long]
) extends Identifiable

case class ProductOptionInput(
  name: String,
  kind: ProductOptionType,
  defaultItemId: Option[Long]
)

sealed trait OptionItem extends Identifiable {
  def optionId: Long
  def id: Long
  def optionType: ProductOptionType
}
case class BasicOptionItem(id: Long, optionId: Long, name: String) extends OptionItem {
  val optionType: ProductOptionType = Basic
}
case class ProductOptionItem(id: Long, optionId: Long, productId: Long) extends OptionItem {
  val optionType: ProductOptionType = Product
}

sealed trait OptionItemInput {
  def optionId: Long
}
case class BasicOptionItemInput(optionId: Long, name: String) extends OptionItemInput
case class ProductOptionItemInput(optionId: Long, productId: Long) extends OptionItemInput

object ProductOptionType extends Enumeration {
  type ProductOptionType = Value
  val Basic = Value("basic")
  val Product = Value("product")
}

case class OptionProduct(
  optionId: Long,
  productId: Long,
  min: Int,
  max: Int
) extends Identifiable {
  def id: (Long, Long) =
    optionId -> productId
}

