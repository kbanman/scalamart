package com.indilago.scalamart.order

import java.util.Currency
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{EntityNotFound, ValidationFailed}
import com.indilago.scalamart.product.configuration.{ProductConfig, ProductConfigurationService}
import com.indilago.scalamart.product.option.price.OptionPriceService
import com.indilago.scalamart.product.price.ProductPriceService
import com.indilago.scalamart.services.ActionNotificationService
import com.indilago.scalamart.util.EventBusHelpers

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultOrderService])
trait OrderService {
  def require(optionId: Long)(implicit ec: ExecutionContext): Future[Order]
  def find(optionId: Long)(implicit ec: ExecutionContext): Future[Option[Order]]
  def create(order: Order)(implicit ec: ExecutionContext): Future[Order]
  def update(order: Order)(implicit ec: ExecutionContext): Future[Order]
  def delete(order: Order)(implicit ec: ExecutionContext): Future[Boolean]
}

@Singleton
class DefaultOrderService @Inject() (
  dao: OrderDao,
  productConfigSvc: ProductConfigurationService,
  priceSvc: ProductPriceService,
  optionPriceSvc: OptionPriceService,
  protected val notifier: ActionNotificationService
) extends OrderService with EventBusHelpers {

  def require(optionId: Long)(implicit ec: ExecutionContext): Future[Order] =
    find(optionId).map(_.getOrElse(throw EntityNotFound(classOf[Order], optionId)))

  def find(optionId: Long)(implicit ec: ExecutionContext): Future[Option[Order]] =
    dao.find(optionId)

  def create(order: Order)(implicit ec: ExecutionContext): Future[Order] = {
    for {
      _ <- assertOptionPricesValid(order)
      created <- dao.create(order)
      _ <- notify(Create, created)
    } yield created
  }

  def update(order: Order)(implicit ec: ExecutionContext): Future[Order] =
    for {
      _ <- assertOptionPricesValid(order)
      updated <- dao.update(order)
      _ <- notify(Update, updated)
    } yield updated

  def delete(order: Order)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      affected <- dao.delete(order)
      wasDeleted = affected > 0
      _ <- notify(Delete, order, wasDeleted)
    } yield wasDeleted

  private def assertOptionPricesValid(order: Order)(implicit ec: ExecutionContext): Future[_] =
    Future.sequence[Unit, Seq](order.items.map(assertOptionPricesValid(order.currency)))

  private def assertOptionPricesValid(currency: Currency)(item: OrderItem)(implicit ec: ExecutionContext): Future[Unit] = {
    def itemIds(config: ProductConfig): Iterable[Long] =
      config.options.flatMap(o => o._2.map(_._1))

    def attachPriceId(itemId: Long): Future[(Long, Long)] =
      optionPriceSvc.currentPrice(itemId, currency).map(p => itemId -> p.id)

    for {
      config <- productConfigSvc.require(item.productConfigId)
      itemPrice <- priceSvc.currentPrice(config.productId, currency)
      optionPrices <- Future.sequence(itemIds(config).map(attachPriceId))
    } yield {
      if (itemPrice.id != item.priceId)
        throw new ValidationFailed(s"Invalid item price. Wanted ${item.priceId}, saw ${itemPrice.id}")
      if (item.optionPrices.toSet != optionPrices.toSet)
        throw new ValidationFailed(s"Invalid option prices. Wanted $optionPrices, saw ${item.optionPrices.toSet}")
    }
  }
}
