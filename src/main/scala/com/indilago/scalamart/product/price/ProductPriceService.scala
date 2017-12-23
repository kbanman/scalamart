package com.indilago.scalamart.product.price

import java.time.{Clock, Instant}
import java.util.Currency
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{EntityNotFound, ValidationFailed}
import com.indilago.scalamart.services._
import com.indilago.scalamart.util.EventBusHelpers

import scala.concurrent.{ExecutionContext, Future}

case class NoProductPriceError(productId: Long, currency: Currency)
  extends EntityNotFound(s"No active ${currency.getCurrencyCode} price for product $productId")

case class ProductPriceValidationError(input: ProductPriceInput, msg: String)
  extends ValidationFailed(s"$input failed validation: $msg")

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
  def delete(price: ProductPrice)(implicit ec: ExecutionContext): Future[Boolean]

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

object ProductPriceService {
  object ErrorMessage {
    val NegativeAmount = "Prices must have a positive amount"
    val DuplicateCardinality = "New prices must not have the same cardinality as an existing price"
  }
}

@Singleton
class DefaultProductPriceService @Inject()(
  dao: ProductPriceDao,
  protected val notifier: ActionNotificationService,
  protected val clock: Clock
) extends ProductPriceService with EventBusHelpers {

  import ProductPriceService.ErrorMessage._

  override def currentPrice(productId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[ProductPrice] =
    dao.activePrice(productId, currency)
      .map(_.getOrElse(throw NoProductPriceError(productId, currency)))

  override def createPrice(input: ProductPriceInput)(implicit ec: ExecutionContext): Future[ProductPrice] =
    for {
      existing <- dao.prices(input.productId)
      _ = assertPositive(input)
      _ = assertUniqueCardinality(input, existing)
      created <- dao.create(priceFromInput(input, determineCardinality(input, existing)))
      _ <- notify(Create, created)
    } yield created

  def delete(price: ProductPrice)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      _ <- assertPriceIsDeletable(price)
      affected <- dao.delete(price)
      wasDeleted = affected > 0
      _ <- notify(Delete, price, wasDeleted)
    } yield wasDeleted

  private def assertPositive(input: ProductPriceInput): Unit =
    if (input.amount < 0)
      throw ProductPriceValidationError(input, NegativeAmount)

  private def assertPriceIsDeletable(price: ProductPrice)(implicit ec: ExecutionContext): Future[_] = Future {
    if (price.start.forall(_.isBefore(Instant.now(clock))))
      throw new ValidationFailed("Cannot delete price that has gone into effect")
  }

  private def assertUniqueCardinality(input: ProductPriceInput, existing: Seq[ProductPrice]): Unit =
    if (input.cardinality.nonEmpty && existing.exists(_.cardinality == input.cardinality.get))
      throw ProductPriceValidationError(input, DuplicateCardinality)

  private def determineCardinality(input: ProductPriceInput, existing: Seq[ProductPrice]): Int =
    existing.headOption.map(_.cardinality).getOrElse(0)
}