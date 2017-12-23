package com.indilago.scalamart.order

import java.time.Instant
import java.util.Currency

import com.indilago.scalamart.product.configuration.ProductConfig
import com.indilago.scalamart.product.price.ProductPrice
import com.indilago.scalamart.testutil.{InjectionHelpers, RandomHelpers, TestClock}
import org.scalatest.concurrent.ScalaFutures

trait OrderHelpers {
  this: RandomHelpers with TestClock with ScalaFutures with InjectionHelpers =>

  val CAD: Currency = Currency.getInstance("CAD")

  def makeOrder(items: Seq[OrderItem], currency: Currency = CAD) =
    Order(
      id = 0,
      created = Instant.now(clock),
      currency = currency,
      items = items
    )

  def makeOrder: Order =
    makeOrder(Seq())

  implicit class OrderImplicits(o: Order) {
    def withoutId: Order =
      o.copy(id = 0, items = o.items.withoutIds)
  }

  implicit class OrderItemListImplicits(list: Seq[OrderItem]) {
    def withoutIds: Seq[OrderItem] =
      list.map(_.copy(id = 0))
  }

  def makeOrderItem: OrderItem =
    OrderItem(
      id = 0,
      productConfigId = positiveLong,
      priceId = positiveLong,
      optionPrices = Map(
        positiveLong -> positiveLong,
        positiveLong -> positiveLong
      )
    )

  def makeOrderItem(config: ProductConfig, price: ProductPrice, optionPrices: Map[Long, Long]): OrderItem =
    OrderItem(
      id = 0,
      productConfigId = config.id,
      priceId = price.id,
      optionPrices = optionPrices
    )
}
