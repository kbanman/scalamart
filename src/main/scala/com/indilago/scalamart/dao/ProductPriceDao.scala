package com.indilago.scalamart.dao

import java.time.{Clock, Instant}
import java.util.Currency

import com.indilago.scalamart.models.product.ProductPrice

import scala.concurrent.{ExecutionContext, Future}

/**
  * Assumptions:
  * - ProductPrice cardinality must be unique for the product, currency
  */
trait ProductPriceDao {

  def clock: Clock

  def activePrice(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[Option[ProductPrice]] =
    activePrices(productId, currency, Instant.now(clock))
      .map(_.headOption)

  def activePrices(productId: Long)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]] =
    prices(productId)
      .map(_.filter(isActivePrice(Instant.now(clock))))

  def activePrices(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]] =
    activePrices(productId, currency, Instant.now(clock))

  def activePrices(productId: Long, currency: Currency, now: Instant)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]] =
    prices(productId).map { res =>
      res.filter(_.currency == currency)
        .filter(isActivePrice(now))
    }

  /**
    * Retrieve all prices for a product, sorted by descending cardinality
    */
  def prices(productId: Long)(implicit ec: ExecutionContext): Future[Seq[ProductPrice]]

  /**
    * Create a new price record, returning the created record
    */
  def createPrice(price: ProductPrice)(implicit ec: ExecutionContext): Future[ProductPrice]

  /**
    * Delete a price record, returning number of affected records
    */
  def deletePrice(priceId: Long)(implicit ec: ExecutionContext): Future[Int]

  private def isActivePrice(now: Instant)(price: ProductPrice): Boolean =
    price.start.forall(_.isBefore(now)) && price.end.forall(_.isAfter(now))
}
