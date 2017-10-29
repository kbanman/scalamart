package com.indilago.scalamart.product.price

import java.time.{Clock, Instant}
import java.util.Currency

import com.indilago.scalamart.util.Crud

import scala.concurrent.{ExecutionContext, Future}

/**
  * Assumptions:
  * - ProductPrice cardinality must be unique for the product, currency
  */
trait ProductPriceDao extends Crud[ProductPrice, Long] {

  protected def clock: Clock

  def activePrice(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[Option[ProductPrice]] =
    activePrices(productId, currency, Instant.now(clock))
      .map(_.headOption)

  def activePrices(productId: Long)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]] =
    prices(productId)
      .map(_.filter(isActive(Instant.now(clock))))

  def activePrices(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]] =
    activePrices(productId, currency, Instant.now(clock))

  def activePrices(productId: Long, currency: Currency, now: Instant)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]] =
    prices(productId).map { res =>
      res.filter(_.currency == currency)
        .filter(isActive(now))
    }

  /**
    * Retrieve all prices for a product, sorted by descending cardinality
    */
  def prices(productId: Long)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]]

  private def isActive(now: Instant)(price: ProductPrice): Boolean =
    price.start.forall(_.isBefore(now)) && price.end.forall(_.isAfter(now))
}
