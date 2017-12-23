package com.indilago.scalamart.order

import java.time.Instant
import java.util.Currency

import com.indilago.scalamart.Identifiable

case class Order(
  id: Long,
  created: Instant,
  currency: Currency,
  items: Seq[OrderItem]
) extends Identifiable

case class OrderItem(
  id: Long,
  productConfigId: Long,
  priceId: Long,
  optionPrices: Map[Long, Long]
)
