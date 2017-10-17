package com.indilago.scalamart.product.price

import java.time.Instant
import java.util.Currency

import com.indilago.scalamart.Identifiable

case class ProductPrice(
  id: Long,
  created: Instant,
  productId: Long,
  currency: Currency,
  amount: BigDecimal,
  cardinality: Int,
  start: Option[Instant],
  end: Option[Instant]
) extends Identifiable
