package com.indilago.scalamart.category

import com.indilago.scalamart.Identifiable

import scala.concurrent.{ExecutionContext, Future}

case class CategoryProduct(
  categoryId: Long,
  productId: Long,
  sort: Int
) extends Identifiable {
  def id: (Long, Long) =
    categoryId -> productId
}

trait CategoryDao {
  def search(id: Long)(implicit ec: ExecutionContext): Future[Option[Category]]
  def search(slug: String)(implicit ec: ExecutionContext): Future[Option[Category]]
  def update(category: Category)(implicit ec: ExecutionContext): Future[Category]
  def delete(id: Long)(implicit ec: ExecutionContext): Future[Int]
  def create(category: Category)(implicit ec: ExecutionContext): Future[Category]
  def addProduct(categoryProduct: CategoryProduct)(implicit ec: ExecutionContext): Future[CategoryProduct]
  def removeProduct(productId: Long, categoryId: Long)(implicit ec: ExecutionContext): Future[Int]
  def updateProduct(categoryProduct: CategoryProduct)(implicit ec: ExecutionContext): Future[CategoryProduct]
  def getProducts(categoryId: Long, recursive: Boolean)(implicit ec: ExecutionContext): Future[Seq[CategoryProduct]]
  def getCategories(productId: Long)(implicit ec: ExecutionContext): Future[Seq[Category]]
  def getChildren(categoryId: Long, recursive: Boolean)(implicit ec: ExecutionContext): Future[Seq[Category]]
}
