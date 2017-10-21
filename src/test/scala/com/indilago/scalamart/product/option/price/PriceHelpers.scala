package com.indilago.scalamart.product.option.price

import java.util.Currency

import com.indilago.scalamart.testutil.RandomHelpers

trait PriceHelpers { this: RandomHelpers =>

  def makePrice(itemId: Long = positiveLong, cardinality: Int = 0) = OptionPrice(
    id = 0,
    itemId = itemId,
    currency = Currency.getInstance("USD"),
    amount = positiveDecimal,
    cardinality = cardinality,
    start = None,
    end = None
  )

  def makePriceInput(price: OptionPrice) = OptionPriceInput(
    itemId = price.itemId,
    currency = price.currency,
    amount = price.amount,
    cardinality = Some(price.cardinality),
    start = price.start,
    end = price.end
  )

  implicit class OptionPriceHelpers(p: OptionPrice) {
    def withoutId: OptionPrice =
      p.copy(id = 0)
  }
}
