package com.indilago.scalamart.product.option.price

import java.time.{Clock, Instant}
import java.util.Currency

import com.indilago.scalamart.util.Crud

import scala.concurrent.{ExecutionContext, Future}

/**
  * Assumptions:
  * - Price cardinality must be unique for the item, currency
  */
trait OptionPriceDao extends Crud[OptionPrice, Long] {
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

  private def isActive(now: Instant)(price: OptionPrice): Boolean =
    price.start.forall(_.isBefore(now)) && price.end.forall(_.isAfter(now))
}
