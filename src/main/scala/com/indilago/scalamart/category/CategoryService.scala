package com.indilago.scalamart.category

import java.time.Clock
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.{BadInput, EntityNotFound}
import com.indilago.scalamart.product.{BasicProduct, ProductService}
import com.indilago.scalamart.services.{ActionNotificationService, ActionType}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultCategoryService])
trait CategoryService {
  /**
    * Find a Category
    * @throws EntityNotFound if missing
    */
  def find(id: Long)(implicit ec: ExecutionContext): Future[Category]
  def find(slug: String)(implicit ec: ExecutionContext): Future[Category]

  /**
    * Find a Category, returning an option
    */
  def maybeFind(id: Long)(implicit ec: ExecutionContext): Future[Option[Category]]
  def maybeFind(slug: String)(implicit ec: ExecutionContext): Future[Option[Category]]

  /**
    * Update a Category
    * @throws BadInput if parent category doesn't exist or is cyclical
    */
  def update(category: Category)(implicit ec: ExecutionContext): Future[Category]

  /**
    * Create a new Category
    * @throws BadInput if parent category doesn't exist
    */
  def create(input: CategoryInput)(implicit ec: ExecutionContext): Future[Category]

  /**
    * Delete a Category
    */
  def delete(category: Category)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    * Add a product to a category
    */
  def addProduct(p: CategoryProduct)(implicit ec: ExecutionContext): Future[_]

  /**
    * Remove a product from a category
    */
  def removeProduct(productId: Long, category: Category)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    * Get products in a category
    */
  def getProducts(category: Category, recursive: Boolean = false)(implicit ec: ExecutionContext): Future[Seq[CategoryProduct]]

  /**
    * Get categories a product belongs to
    */
  def getCategories(productId: Long)(implicit ec: ExecutionContext): Future[Seq[Category]]

  /**
    * Get child categories
    */
  def getChildren(category: Category, recursive: Boolean = false)(implicit ec: ExecutionContext): Future[Seq[Category]]
}


@Singleton
class DefaultCategoryService @Inject()(
  dao: CategoryDao,
  notifier: ActionNotificationService,
  clock: Clock
) extends CategoryService {

  def find(id: Long)(implicit ec: ExecutionContext): Future[Category] =
    maybeFind(id).map(_.getOrElse(throw EntityNotFound(classOf[Category], id)))

  def find(slug: String)(implicit ec: ExecutionContext): Future[Category] =
    maybeFind(slug).map(_.getOrElse(throw EntityNotFound(classOf[Category], slug)))

  def maybeFind(id: Long)(implicit ec: ExecutionContext): Future[Option[Category]] =
    dao.search(id)

  def maybeFind(slug: String)(implicit ec: ExecutionContext): Future[Option[Category]] =
    dao.search(slug)

  def update(category: Category)(implicit ec: ExecutionContext): Future[Category] =
    for {
      _ <- assertAcyclical(category)
      updated <- dao.update(category)
      _ <- notifier.recordAction(ActionType.Update, updated)
    } yield updated

  def create(input: CategoryInput)(implicit ec: ExecutionContext): Future[Category] = {
    val category = input2Category(input)
    for {
      _ <- assertParentExists(category)
      created <- dao.create(category)
      _ <- notifier.recordAction(ActionType.Create, created)
    } yield created
  }

  def delete(category: Category)(implicit ec: ExecutionContext): Future[Boolean] =
    dao.delete(category.id).map { affected =>
      if (affected > 0) {
        notifier.recordAction(ActionType.Delete, category)
        true
      } else false
    }

  def addProduct(p: CategoryProduct)(implicit ec: ExecutionContext): Future[CategoryProduct] =
    for {
      created <- dao.addProduct(p)
      _ <- notifier.recordAction(ActionType.Create, created)
    } yield created

  def removeProduct(productId: Long, category: Category)(implicit ec: ExecutionContext): Future[Boolean] =
    dao.removeProduct(productId, category.id).map { affected =>
      if (affected > 0) {
        notifier.recordAction(ActionType.Delete, CategoryProduct(category.id, productId, 0))
        true
      } else false
    }

  def updateProduct(p: CategoryProduct)(implicit ec: ExecutionContext): Future[CategoryProduct] =
    for {
      updated <- dao.updateProduct(p)
      _ <- notifier.recordAction(ActionType.Update, updated)
    } yield updated


  def getProducts(category: Category, recursive: Boolean)(implicit ec: ExecutionContext): Future[Seq[CategoryProduct]] =
    dao.getProducts(category.id, recursive)

  def getCategories(productId: Long)(implicit ec: ExecutionContext): Future[Seq[Category]] =
    dao.getCategories(productId)

  def getChildren(category: Category, recursive: Boolean)(implicit ec: ExecutionContext): Future[Seq[Category]] =
    dao.getChildren(category.id, recursive)

  private def assertParentExists(category: Category)(implicit ec: ExecutionContext): Future[_] =
    category.parentCategoryId match {
      case Some(id) =>
        dao.search(id).map(_.getOrElse(throw BadInput("Parent category does not exist")))
      case None =>
        Future.successful({})
    }

  private def assertAcyclical(category: Category)(implicit ec: ExecutionContext): Future[_] =
    dao.getChildren(category.id, recursive = true).map { res =>
      if (res.map(_.id).toSet.contains(category.parentCategoryId))
        throw BadInput("Cyclical category tree detected")
    }

  private def input2Category(input: CategoryInput): Category =
    Category(
      id = 0,
      slug = input.slug,
      parentCategoryId = input.parentCategoryId,
      name = input.name,
      description = input.description,
    )
}
