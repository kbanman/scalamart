package com.indilago.scalamart.services

import java.time.{Clock, Instant}
import java.util.Currency
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.dao.ProductPriceDao
import com.indilago.scalamart.models.product.ProductPrice

import scala.concurrent.{ExecutionContext, Future}

case class NoProductPriceError(productId: Long, currency: Currency)
  extends RuntimeException(s"No active ${currency.getCurrencyCode} price for product $productId")

case class ProductPriceValidationError(input: ProductPriceInput, message: String)
  extends RuntimeException(s"$input failed validation: $message")

case class ProductPriceInput(
  productId: Long,
  currency: Currency,
  amount: BigDecimal,
  cardinality: Option[Int],
  start: Option[Instant],
  end: Option[Instant]
)

@ImplementedBy(classOf[DefaultProductPriceService])
trait ProductPriceService {
  /**
    * Injected clock for testing
    */
  protected def clock: Clock

  /**
    * Get the current price for a product, currency
    * @throws NoProductPriceError if none exists
    */
  def currentPrice(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[ProductPrice]

  /**
    * Create a new price
    * @throws ProductPriceValidationError for invalid input
    */
  def createPrice(input: ProductPriceInput)(implicit ec: ExecutionContext): Future[ProductPrice]

  /**
    * Delete a price
    */
  def deletePrice(productPrice: ProductPrice)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    * Create a price from ProductPriceInput
    */
  protected def priceFromInput(input: ProductPriceInput, cardinality: Int) = ProductPrice(
    id = 0,
    created = Instant.now(clock),
    productId = input.productId,
    currency = input.currency,
    amount = input.amount,
    cardinality = cardinality,
    start = input.start,
    end = input.end
  )
}

@Singleton
class DefaultProductPriceService @Inject()(
  dao: ProductPriceDao,
  notifier: ActionNotificationService,
  protected val clock: Clock
) extends ProductPriceService {

  override def currentPrice(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[ProductPrice] =
    dao.activePrice(productId, currency)
      .map(_.getOrElse(throw NoProductPriceError(productId, currency)))

  override def createPrice(input: ProductPriceInput)(implicit ec: ExecutionContext): Future[ProductPrice] =
    for {
      existing <- dao.prices(input.productId)
      _ = assertPositive(input)
      _ = assertUniqueCardinality(input, existing)
      created <- dao.createPrice(priceFromInput(input, determineCardinality(input, existing)))
      _ <- notifier.recordAction(ActionType.Create, classOf[ProductPrice], created.id)
    } yield created

  def deletePrice(productPrice: ProductPrice)(implicit ec: ExecutionContext): Future[Boolean] =
    dao.deletePrice(productPrice.id).map { affected =>
      if (affected > 0) {
        notifier.recordAction(ActionType.Delete, classOf[ProductPrice], productPrice.id)
        true
      } else false
    }

  private def assertPositive(input: ProductPriceInput): Unit =
    if (input.amount < 0)
      throw ProductPriceValidationError(input, "Prices must have a positive amount")

  private def assertUniqueCardinality(input: ProductPriceInput, existing: Seq[ProductPrice]): Unit =
    if (input.cardinality.nonEmpty && existing.exists(_.cardinality == input.cardinality.get))
      throw ProductPriceValidationError(input, "New prices must not have the same cardinality as an existing price")

  private def determineCardinality(input: ProductPriceInput, existing: Seq[ProductPrice]): Int =
    existing.headOption.map(_.cardinality).getOrElse(0)
}