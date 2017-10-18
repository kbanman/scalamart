package com.indilago.scalamart.product.option

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.product.option.ProductOptionType.ProductOptionType

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
}
case class BasicOptionItem(id: Long, optionId: Long, name: String) extends OptionItem
case class ProductOptionItem(id: Long, optionId: Long, productId: Long) extends OptionItem

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
