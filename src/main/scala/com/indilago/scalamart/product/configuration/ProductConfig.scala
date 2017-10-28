package com.indilago.scalamart.product.configuration

import com.indilago.scalamart.Identifiable

case class ProductConfig(
  id: Long,
  productId: Long,
  // Map[OptionId, Seq[(ItemId, quantity)]]
  options: Map[Long, Seq[(Long, Int)]],
) extends Identifiable
