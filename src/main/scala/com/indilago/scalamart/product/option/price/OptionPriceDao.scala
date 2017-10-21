package com.indilago.scalamart.product.option.price

import java.time.{Clock, Instant}
import java.util.Currency

import scala.concurrent.{ExecutionContext, Future}

/**
  * Assumptions:
  * - Price cardinality must be unique for the item, currency
  */
trait OptionPriceDao {
  protected def clock: Clock

  def activePrice(itemId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[Option[OptionPrice]] =
    activePrices(itemId, currency, Instant.now(clock))
      .map(_.headOption)

  def activePrices(itemId: Long)(implicit ec: ExecutionContext): Future[Seq[OptionPrice]] =
    prices(itemId)
      .map(_.filter(isActive(Instant.now(clock))))

  def activePrices(itemId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[Seq[OptionPrice]] =
    activePrices(itemId, currency, Instant.now(clock))

  def activePrices(itemId: Long, currency: Currency, now: Instant)(implicit ec: ExecutionContext): Future[Seq[OptionPrice]] =
    prices(itemId).map { res =>
      res.filter(_.currency == currency)
        .filter(isActive(now))
    }

  /**
    * Retrieve all prices for an item, sorted by descending cardinality
    */
  def prices(itemId: Long)(implicit ec: ExecutionContext): Future[Seq[OptionPrice]]

  /**
    * Create a new price record, returning the created record
    */
  def create(price: OptionPrice)(implicit ec: ExecutionContext): Future[OptionPrice]

  /**
    * Delete a price record, returning number of affected records
    */
  def delete(priceId: Long)(implicit ec: ExecutionContext): Future[Int]

  private def isActive(now: Instant)(price: OptionPrice): Boolean =
    price.start.forall(_.isBefore(now)) && price.end.forall(_.isAfter(now))
}
