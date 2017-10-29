package com.indilago.scalamart.category

import com.indilago.scalamart.Identifiable
import com.indilago.scalamart.util.Crud

import scala.concurrent.{ExecutionContext, Future}

case class CategoryProduct(
  categoryId: Long,
  productId: Long,
  sort: Int
) extends Identifiable {
  def id: (Long, Long) =
    categoryId -> productId
}

trait CategoryDao extends Crud[Category, Long] {
  def find(slug: String)(implicit ec: ExecutionContext): Future[Option[Category]]
  def addProduct(categoryProduct: CategoryProduct)(implicit ec: ExecutionContext): Future[CategoryProduct]
  def removeProduct(productId: Long, categoryId: Long)(implicit ec: ExecutionContext): Future[Int]
  def updateProduct(categoryProduct: CategoryProduct)(implicit ec: ExecutionContext): Future[CategoryProduct]
  def getProducts(categoryId: Long, recursive: Boolean)(implicit ec: ExecutionContext): Future[Seq[CategoryProduct]]
  def getCategories(productId: Long)(implicit ec: ExecutionContext): Future[Seq[Category]]
  def getChildren(categoryId: Long, recursive: Boolean)(implicit ec: ExecutionContext): Future[Seq[Category]]
}
