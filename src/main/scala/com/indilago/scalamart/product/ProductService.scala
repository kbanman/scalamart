package com.indilago.scalamart.product

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.indilago.scalamart.exception.EntityNotFound
import com.indilago.scalamart.services.{ActionNotificationService, ActionType}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultProductService])
trait ProductService {
  /**
    * Find a product
    * @throws EntityNotFound if missing
    */
  def find(id: Long)(implicit ec: ExecutionContext): Future[BasicProduct]

  /**
    * Find a product, returning an option
    */
  def maybeFind(id: Long)(implicit ec: ExecutionContext): Future[Option[BasicProduct]]

  /**
    * Update a product
    */
  def update(product: BasicProduct)(implicit ec: ExecutionContext): Future[BasicProduct]

  /**
    * Create a new product
    */
  def create(input: ProductInput)(implicit ec: ExecutionContext): Future[BasicProduct]

  /**
    * Delete a product
    */
  def delete(product: BasicProduct)(implicit ec: ExecutionContext): Future[Boolean]
}


@Singleton
class DefaultProductService @Inject()(
  dao: ProductDao,
  notifier: ActionNotificationService,
  clock: Clock
) extends ProductService {

  def find(id: Long)(implicit ec: ExecutionContext): Future[BasicProduct] =
    maybeFind(id).map(_.getOrElse(throw EntityNotFound(classOf[BasicProduct], id)))

  def maybeFind(id: Long)(implicit ec: ExecutionContext): Future[Option[BasicProduct]] =
    dao.search(id)

  def update(product: BasicProduct)(implicit ec: ExecutionContext): Future[BasicProduct] =
    dao.update(product)

  def create(input: ProductInput)(implicit ec: ExecutionContext): Future[BasicProduct] =
    dao.create(input2Product(input)).map { created =>
      notifier.recordAction(ActionType.Create, created)
      created
    }

  def delete(product: BasicProduct)(implicit ec: ExecutionContext): Future[Boolean] =
    dao.delete(product.id).map { affected =>
      if (affected > 0) {
        notifier.recordAction(ActionType.Delete, product)
        true
      } else false
    }

  private def input2Product(input: ProductInput): BasicProduct =
    BasicProduct(
      id = 0,
      created = Instant.now(clock),
      name = input.name,
      description = input.description,
      availabilityStart = input.availabilityStart,
      availabilityEnd = input.availabilityEnd
    )
}
