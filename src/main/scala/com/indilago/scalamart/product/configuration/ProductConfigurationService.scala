package com.indilago.scalamart.product.configuration

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{EntityNotFound, ValidationFailed}
import com.indilago.scalamart.product.{BaseProduct, ProductService}
import com.indilago.scalamart.product.option.{OptionItem, OptionProduct, ProductOption, ProductOptionService}
import com.indilago.scalamart.services.ActionType.{Create, Delete, Update}
import com.indilago.scalamart.services.{ActionNotificationService, ActionType}
import com.indilago.scalamart.util.SerialFutures.SerialFutureSeq

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultProductConfigurationService])
trait ProductConfigurationService {
  def require(configId: Long)(implicit ec: ExecutionContext): Future[ProductConfig]
  def find(configId: Long)(implicit ec: ExecutionContext): Future[Option[ProductConfig]]
  def create(config: ProductConfig)(implicit ec: ExecutionContext): Future[ProductConfig]
  def update(config: ProductConfig)(implicit ec: ExecutionContext): Future[ProductConfig]
  def delete(config: ProductConfig)(implicit ec: ExecutionContext): Future[Boolean]
}

object ProductConfigurationService {
  object ErrorMessage {
    def ItemQuantityOutOfBounds(qty: Int, op: OptionProduct) =
      s"Item quantity $qty is outside the bounds of ${op.min} and ${op.max}"
  }
}

@Singleton
class DefaultProductConfigurationService @Inject() (
  dao: ProductConfigurationDao,
  productSvc: ProductService,
  optionSvc: ProductOptionService,
  notifier: ActionNotificationService
) extends ProductConfigurationService {

  import ProductConfigurationService.ErrorMessage._

  case class OptionDescriptor(option: ProductOption, op: OptionProduct)

  def require(configId: Long)(implicit ec: ExecutionContext): Future[ProductConfig] =
    find(configId).map(_.getOrElse(throw EntityNotFound(classOf[ProductConfig], configId)))

  def find(configId: Long)(implicit ec: ExecutionContext): Future[Option[ProductConfig]] =
    dao.find(configId)

  def create(config: ProductConfig)(implicit ec: ExecutionContext): Future[ProductConfig] =
    for {
      _ <- assertValid(config)
      created <- dao.create(config)
      _ <- notifier.recordAction(Create, created)
    } yield created

  def update(config: ProductConfig)(implicit ec: ExecutionContext): Future[ProductConfig] =
    for {
      _ <- assertValid(config)
      updated <- dao.update(config)
      _ <- notifier.recordAction(Update, updated)
    } yield updated

  def delete(config: ProductConfig)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      affected <- dao.delete(config)
      changed = affected > 0
      _ <- if (changed) notifier.recordAction(Delete, config) else Future.successful({})
    } yield changed

  private def assertValid(config: ProductConfig)(implicit ec: ExecutionContext): Future[_] =
    for {
      product <- productSvc.require(config.productId)
      options <- loadOptions(product, config.options)
    } yield options.foreach {
      case (od, items) =>
        validateItemQty(items, od.op)
    }

  private def validateItemQty(items: Seq[(OptionItem, Int)], op: OptionProduct): Unit = {
    val qty = items.foldLeft(0) {
      case (sum, (_, itemQty)) => sum + itemQty
    }
    if (qty < op.min || qty > op.max)
      throw new ValidationFailed(ItemQuantityOutOfBounds(qty, op))
  }

  type ItemIdQty = (Long, Int)
  type ItemQty = (OptionItem, Int)

  private def loadOptions(product: BaseProduct, options: Map[Long, Seq[ItemIdQty]])(implicit ec: ExecutionContext): Future[Map[OptionDescriptor, Seq[ItemQty]]] =
    options.toSeq.serialMap(loadOption(product))
      .map(_.toMap)

  private def loadOption(product: BaseProduct)(implicit ec: ExecutionContext): ((Long, Seq[ItemIdQty])) => Future[(OptionDescriptor, Seq[ItemQty])] = {
    case (optionId, itemIds) =>
      for {
        option <- optionSvc.require(optionId)
        op <- optionSvc.findOption(product, option).map(_.get) // @todo better error handling
        items <- itemIds.serialMap[ItemQty](loadItem(option))
      } yield {
        OptionDescriptor(option, op) -> items
      }
  }

  private def loadItem(option: ProductOption)(implicit ex: ExecutionContext): ItemIdQty => Future[ItemQty] = {
    case (itemId, quantity) =>
      optionSvc.findItem(option, itemId).map(_ -> quantity)
  }
}