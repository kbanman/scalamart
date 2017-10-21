package com.indilago.scalamart.product.option.price

import java.time.Instant
import java.util.Currency

import com.indilago.scalamart.Identifiable

case class OptionPrice(
  id: Long,
  itemId: Long,
  currency: Currency,
  amount: BigDecimal,
  cardinality: Int,
  start: Option[Instant],
  end: Option[Instant]
) extends Identifiable
