package com.indilago.scalamart.product.option

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{BadInput, EntityNotFound, PreconditionFailed}
import com.indilago.scalamart.product.BaseProduct
import com.indilago.scalamart.product.option.ProductOptionType.{Basic, Product}
import com.indilago.scalamart.services.ActionNotificationService
import com.indilago.scalamart.services.ActionType.ActionType
import com.indilago.scalamart.util.EventBusHelpers

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultProductOptionService])
trait ProductOptionService {
  def getOptions(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]]
  def findOption(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Option[OptionProduct]]
  def addOption(op: OptionProduct)(implicit ec: ExecutionContext): Future[Boolean]
  def updateOption(op: OptionProduct)(implicit ec: ExecutionContext): Future[OptionProduct]
  def removeOption(op: OptionProduct)(implicit ec: ExecutionContext): Future[Boolean]
  def require(optionId: Long)(implicit ec: ExecutionContext): Future[ProductOption]
  def find(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]]
  def create(input: ProductOptionInput)(implicit ec: ExecutionContext): Future[ProductOption]
  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption]
  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean]
  def getItems(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItem]]
  def findItem(option: ProductOption, itemId: Long)(implicit ec: ExecutionContext): Future[OptionItem]
  def createItem(input: OptionItemInput)(implicit ec: ExecutionContext): Future[OptionItem]
  def updateItem(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItem]
  def deleteItem(item: OptionItem)(implicit ec: ExecutionContext): Future[Boolean]
}

object ProductOptionService {
  object ErrorMessage {
    val NonExistentOption = "Option doesn't exist"
    val NonExistentItem = "Item doesn't exist"
    val InvalidItemType = "Invalid item type"
    val InvalidDefaultItem = "Default item must belong to the option"
    val ItemsChangingType = "Must remove all items before changing option type"
    val CannotChangeItemOption = "Cannot change the option an item belongs to"
  }
}

@Singleton
class DefaultProductOptionService @Inject()(
  optionDao: ProductOptionDao,
  itemDao: ProductOptionItemDao,
  protected val notifier: ActionNotificationService
) extends ProductOptionService with EventBusHelpers {

  import ProductOptionService.ErrorMessage._

  def getOptions(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]] =
    optionDao.optionsForProduct(product)

  def findOption(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Option[OptionProduct]] =
    optionDao.findOptionProduct(product, option)

  def addOption(op: OptionProduct)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      affected <- optionDao.addOptionProduct(op)
      changed = affected > 0
      _ <- notify(Create, op, changed)
    } yield changed

  def updateOption(op: OptionProduct)(implicit ec: ExecutionContext): Future[OptionProduct] =
    for {
      updated <- optionDao.updateOptionProduct(op)
      _ <- notify(Update, op)
    } yield updated

  def removeOption(op: OptionProduct)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      affected <- optionDao.removeOptionProduct(op)
      changed = affected > 0
      _ <- notify(Delete, op, changed)
    } yield changed

  def require(optionId: Long)(implicit ec: ExecutionContext): Future[ProductOption] =
    find(optionId).map(_.getOrElse(throw EntityNotFound(classOf[ProductOption], optionId)))

  def find(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]] =
    optionDao.find(optionId)

  def create(input: ProductOptionInput)(implicit ec: ExecutionContext): Future[ProductOption] = {
    val option = toOption(input)
    for {
      _ <- assertDefaultItemExists(option)
      created <- optionDao.create(option)
      _ <- notify(Create, created)
    } yield created
  }

  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption] =
    for {
      existing <- optionDao.find(option.id)
      _ <- assertDefaultItemExists(option)
      _ <- assertNoItemsIfChangingType(existing.get, option)
      updated <- optionDao.update(option)
      _ <- notify(Update, updated)
    } yield updated

  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      items <- getItems(option)
      _ <- Future.sequence(items.map(deleteItem))
      affected <- optionDao.delete(option)
      wasDeleted = affected > 0
      _ <- notify(Delete, option, wasDeleted)
    } yield wasDeleted

  def getItems(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItem]] =
    itemDao.itemsForOption(option)
      .map(_.map(r => toItem(option, r)))

  def findItem(option: ProductOption, itemId: Long)(implicit ec: ExecutionContext): Future[OptionItem] =
    itemDao.find(itemId)
      .map(_.getOrElse(throw EntityNotFound(classOf[OptionItem], itemId)))
      .map(r => toItem(option, r))

  def createItem(input: OptionItemInput)(implicit ec: ExecutionContext): Future[OptionItem] =
    for {
      maybeOption <- optionDao.find(input.optionId)
      option = maybeOption.getOrElse(throw BadInput(NonExistentOption))
      item = toItem(input)
      _ = if (item.optionType != option.kind) throw BadInput(InvalidItemType)
      createdRecord <- itemDao.create(toItemRecord(item))
      createdItem = toItem(option, createdRecord)
      _ <- notifyItem(Create, createdItem)
    } yield createdItem

  def updateItem(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItem] =
    for {
      maybeOption <- optionDao.find(item.optionId)
      option = maybeOption.getOrElse(throw BadInput(NonExistentOption))
      maybeExisting <- itemDao.find(item.id)
      existing = maybeExisting.getOrElse(throw EntityNotFound(classOf[OptionItem], item.id))
      _ = if (item.optionId != existing.optionId) throw BadInput(CannotChangeItemOption)
      _ = if (item.optionType != option.kind) throw BadInput(InvalidItemType)
      updatedRecord <- itemDao.update(toItemRecord(item))
      updatedItem = toItem(option, updatedRecord)
      _ <- notifyItem(Update, updatedItem)
    } yield updatedItem

  def deleteItem(item: OptionItem)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      affected <- itemDao.delete(toItemRecord(item))
      wasDeleted = affected > 0
      _ <- notifyItem(Delete, item, wasDeleted)
    } yield wasDeleted

  private def assertDefaultItemExists(option: ProductOption)(implicit ec: ExecutionContext): Future[_] =
    option.defaultItemId match {
      case Some(itemId) =>
        itemDao.itemsForOption(option).map { items =>
          items.find(_.id == itemId).getOrElse {
            throw new BadInput(InvalidDefaultItem)
          }
        }
      case None =>
        Future.successful({})
    }

  private def assertNoItemsIfChangingType(existing: ProductOption, updated: ProductOption)(implicit ec: ExecutionContext): Future[_] =
    if (existing.kind != updated.kind) {
      itemDao.itemsForOption(existing).map { items =>
        if (items.nonEmpty)
          throw new PreconditionFailed(ItemsChangingType)
      }
    } else Future.successful({})

  private def toOption: ProductOptionInput => ProductOption = {
    case ProductOptionInput(name, kind, defaultItemId) =>
      ProductOption(0, name, kind, defaultItemId)
  }

  private def toItem: OptionItemInput => OptionItem = {
    case BasicOptionItemInput(optionId, name) =>
      BasicOptionItem(0, optionId, name)
    case ProductOptionItemInput(optionId, productId) =>
      ProductOptionItem(0, optionId, productId)
  }

  private def toItemRecord: OptionItem => OptionItemRecord = {
    case BasicOptionItem(id, optionId, name) =>
      OptionItemRecord(id, optionId, Some(name), None)
    case ProductOptionItem(id, optionId, productId) =>
      OptionItemRecord(id, optionId, None, Some(productId))
  }

  private def toItem(o: ProductOption, r: OptionItemRecord): OptionItem =
    o.kind match {
      case Basic =>
        BasicOptionItem(r.id, r.optionId, r.name.get)
      case Product =>
        ProductOptionItem(r.id, r.optionId, r.productId.get)
    }

  // @todo check if this is still needed
  private def notifyItem(action: ActionType, item: OptionItem, shouldNotify: Boolean = true)(implicit ec: ExecutionContext): Future[_] =
    if (shouldNotify)
      notifier.recordAction(action, classOf[OptionItem], item.id)
    else
      Future.successful({})
}
