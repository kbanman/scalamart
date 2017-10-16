package com.indilago.scalamart.models.product

import java.time.Instant
import java.util.Currency

case class ProductPrice(
  id: Long,
  created: Instant,
  productId: Long,
  currency: Currency,
  amount: BigDecimal,
  cardinality: Int,
  start: Option[Instant],
  end: Option[Instant]
)
