package com.indilago.scalamart.product.option.price

import java.time.{Clock, Instant}
import java.util.Currency
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{EntityNotFound, ValidationFailed}
import com.indilago.scalamart.services._

import scala.concurrent.{ExecutionContext, Future}

case class NoOptionPriceError(itemId: Long, currency: Currency)
  extends EntityNotFound(s"No active ${currency.getCurrencyCode} price for item $itemId")

case class OptionPriceValidationError(input: OptionPriceInput, msg: String)
  extends ValidationFailed(s"$input failed validation: $msg")

case class OptionPriceInput(
  itemId: Long,
  currency: Currency,
  amount: BigDecimal,
  cardinality: Option[Int],
  start: Option[Instant],
  end: Option[Instant]
)

@ImplementedBy(classOf[DefaultOptionPriceService])
trait OptionPriceService {
  /**
    * Injected clock for testing
    */
  protected def clock: Clock

  /**
    * Get the current price for an item, currency
    * @throws NoOptionPriceError if none exists
    */
  def currentPrice(itemId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[OptionPrice]

  /**
    * Create a new price
    * @throws OptionPriceValidationError for invalid input
    */
  def createPrice(input: OptionPriceInput)(implicit ec: ExecutionContext): Future[OptionPrice]

  /**
    * Delete a price
    */
  def delete(price: OptionPrice)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    * Create a price from input
    */
  protected def priceFromInput(input: OptionPriceInput, cardinality: Int) = OptionPrice(
    id = 0,
    itemId = input.itemId,
    currency = input.currency,
    amount = input.amount,
    cardinality = cardinality,
    start = input.start,
    end = input.end
  )
}

object OptionPriceService {
  object ErrorMessage {
    val NegativeAmount = "Prices must have a positive amount"
    val DuplicateCardinality = "New prices must not have the same cardinality as an existing price"
  }
}

@Singleton
class DefaultOptionPriceService @Inject()(
  dao: OptionPriceDao,
  notifier: ActionNotificationService,
  protected val clock: Clock
) extends OptionPriceService {
  import OptionPriceService.ErrorMessage._

  override def currentPrice(itemId: Long, currency: Currency)(implicit ec: ExecutionContext): Future[OptionPrice] =
    dao.activePrice(itemId, currency)
      .map(_.getOrElse(throw NoOptionPriceError(itemId, currency)))

  override def createPrice(input: OptionPriceInput)(implicit ec: ExecutionContext): Future[OptionPrice] =
    for {
      existing <- dao.prices(input.itemId)
      _ = assertPositive(input)
      _ = assertUniqueCardinality(input, existing)
      created <- dao.create(priceFromInput(input, determineCardinality(input, existing)))
      _ <- notifier.recordAction(ActionType.Create, classOf[OptionPrice], created.id)
    } yield created

  def delete(price: OptionPrice)(implicit ec: ExecutionContext): Future[Boolean] =
    dao.delete(price.id).map { affected =>
      if (affected > 0) {
        notifier.recordAction(ActionType.Delete, price)
        true
      } else false
    }

  private def assertPositive(input: OptionPriceInput): Unit =
    if (input.amount < 0)
      throw OptionPriceValidationError(input, NegativeAmount)

  private def assertUniqueCardinality(input: OptionPriceInput, existing: Seq[OptionPrice]): Unit =
    if (input.cardinality.nonEmpty && existing.exists(_.cardinality == input.cardinality.get))
      throw OptionPriceValidationError(input, DuplicateCardinality)

  private def determineCardinality(input: OptionPriceInput, existing: Seq[OptionPrice]): Int =
    existing.headOption.map(_.cardinality).getOrElse(0)
}