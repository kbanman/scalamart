package com.indilago.scalamart.product.option

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{BadInput, EntityNotFound}
import com.indilago.scalamart.product.BaseProduct
import com.indilago.scalamart.product.option.ProductOptionType.{Basic, Product}
import com.indilago.scalamart.services.ActionNotificationService
import com.indilago.scalamart.services.ActionType._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultProductOptionService])
trait ProductOptionService {
  def getOptions(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]]
  def addOption(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean]
  def removeOption(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean]
  def find(optionId: Long)(implicit ec: ExecutionContext): Future[ProductOption]
  def search(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]]
  def create(option: ProductOptionInput)(implicit ec: ExecutionContext): Future[ProductOption]
  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption]
  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean]
  def getItems(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItem]]
  def createItem(input: OptionItemInput)(implicit ec: ExecutionContext): Future[OptionItem]
  def updateItem(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItem]
  def deleteItem(item: OptionItem)(implicit ec: ExecutionContext): Future[Boolean]
}

@Singleton
class DefaultProductOptionService @Inject()(
  dao: ProductOptionDao,
  notifier: ActionNotificationService
) extends ProductOptionService {

  def getOptions(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]] =
    dao.optionsForProduct(product)

  def addOption(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean] = {
    val op = OptionProduct(product.id, option.id)
    for {
      affected <- dao.addOptionProduct(op)
      changed = affected > 0
      _ <- notifier.recordAction(Create, op)
    } yield changed
  }

  def removeOption(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean] = {
    val op = OptionProduct(product.id, option.id)
    for {
      affected <- dao.removeOptionProduct(op)
      changed = affected > 0
      _ <- notifier.recordAction(Delete, op)
    } yield changed
  }

  def find(optionId: Long)(implicit ec: ExecutionContext): Future[ProductOption] =
    search(optionId).map(_.getOrElse(throw EntityNotFound(classOf[ProductOption], optionId)))

  def search(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]] =
    dao.search(optionId)

  def create(option: ProductOptionInput)(implicit ec: ExecutionContext): Future[ProductOption] =
    for {
      created <- dao.create(toOption(option))
      _ <- notifier.recordAction(Create, created)
    } yield created

  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption] =
    for {
      updated <- dao.update(option)
      _ <- notifier.recordAction(Update, updated)
    } yield updated

  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      items <- getItems(option)
      _ <- Future.sequence(items.map(dao.deleteItem))
      affected <- dao.delete(option)
      wasDeleted = affected > 0
      _ <- notifier.recordAction(Delete, classOf[ProductOption], option) if wasDeleted
    } yield wasDeleted

  def getItems(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItem]] =
    dao.itemsForOption(option).map(_.map(r => toItem(option, r)))

  def createItem(input: OptionItemInput)(implicit ec: ExecutionContext): Future[OptionItem] =
    for {
      maybeOption <- dao.search(input.optionId)
      option = maybeOption.getOrElse(throw new BadInput("Option doesn't exist"))
      createdRecord <- dao.createItem(toItem(input))
      createdItem = toItem(option, createdRecord)
      _ <- notifier.recordAction(Create, createdItem)
    } yield createdItem

  def updateItem(item: OptionItem)(implicit ec: ExecutionContext): Future[OptionItem] =
    for {
      maybeOption <- dao.search(item.optionId)
      option = maybeOption.getOrElse(throw BadInput("Option doesn't exist"))
      updatedRecord <- dao.updateItem(item)
      updatedItem = toItem(option, updatedRecord)
      _ <- notifier.recordAction(Update, updatedItem)
    } yield updatedItem

  def deleteItem(item: OptionItem)(implicit ec: ExecutionContext): Future[Boolean] =
    for {
      affected <- dao.deleteItem(item)
      wasDeleted = affected > 0
      _ <- notifier.recordAction(Delete, item) if wasDeleted
    } yield wasDeleted

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

  private def toItem(o: ProductOption, r: OptionItemRecord): OptionItem =
    o.kind match {
      case Basic =>
        BasicOptionItem(r.id, r.optionId, r.name.get)
      case Product =>
        ProductOptionItem(r.id, r.optionId, r.productId.get)
    }
}
