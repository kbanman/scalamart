package com.indilago.scalamart.category

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

class FakeCategoryDao extends CategoryDao {

  @volatile
  private var records = Seq[Category]()

  @volatile
  private var products = Seq[CategoryProduct]()

  override def create(category: Category)(implicit ec: ExecutionContext) = Future {
    val record = category.copy(id = records.length + 1)
    records = records :+ record
    record
  }

  override def delete(id: Long)(implicit ec: ExecutionContext) = Future {
    val affected = records.count(_.id == id)
    records = records.filterNot(_.id == id)
    affected
  }

  override def search(id: Long)(implicit ec: ExecutionContext) = Future {
    records.find(_.id == id)
  }

  override def search(slug: String)(implicit ec: ExecutionContext) = Future {
    records.find(_.slug == slug)
  }

  override def update(category: Category)(implicit ec: ExecutionContext) = Future {
    val existing = records.find(_.id == category.id)
      .getOrElse(throw new RuntimeException(s"Cannot update a nonexistent $category"))
    records = records.updated(records.indexOf(existing), category)
    category
  }

  def addProduct(cp: CategoryProduct)(implicit ec: ExecutionContext) = Future {
    products.find(sameId(cp)) match {
      case Some(_) =>
        throw new RuntimeException(s"Duplicate foreign key error for $cp")
      case None =>
        shiftProductSort(cp.categoryId, cp.sort)
        products = products :+ cp
        cp
    }
  }

  def updateProduct(categoryProduct: CategoryProduct)(implicit ec: ExecutionContext) = Future {
    doUpdateProduct(categoryProduct)
  }

  private def sameId(a: CategoryProduct)(b: CategoryProduct): Boolean =
    a.productId == b.productId && a.categoryId == b.categoryId

  private def doUpdateProduct(cp: CategoryProduct): CategoryProduct = {
    val existing = products.find(sameId(cp))
        .getOrElse(throw new RuntimeException(s"Cannot update nonexistent $cp"))
    products = products.updated(products.indexOf(existing), cp)
    cp
  }

  private def shiftProductSort(categoryId: Long, offset: Int): Unit = {
    products.filter(_.categoryId == categoryId)
      .sortBy(_.sort)
      .drop(offset)
      .map(cp => cp.copy(sort = cp.sort+1))
      .map(doUpdateProduct)
  }

  def removeProduct(productId: Long, categoryId: Long)(implicit ec: ExecutionContext) = Future {
    products.find(p => p.productId == productId && p.categoryId == categoryId) match {
      case Some(cp) =>
        products = products.filterNot(_ == cp)
        1
      case None =>
        0
    }
  }

  def getProducts(categoryId: Long, recursive: Boolean)(implicit ec: ExecutionContext) = Future {
    doGetProducts(categoryId, recursive)
  }

  private def doGetProducts(categoryId: Long, recursive: Boolean) = {
    val cats =
      if (recursive)
        getChildrenRecursive(categoryId).toSet + categoryId
      else
        Set(categoryId)
    products.filter(p => cats.contains(p.categoryId))
      .sortBy(_.sort)
  }

  def getCategories(productId: Long)(implicit ec: ExecutionContext) = Future {
    products.filter(_.productId == productId)
      .map(_.categoryId)
      .map(find)
  }

  def getChildren(categoryId: Long, recursive: Boolean)(implicit ec: ExecutionContext) = Future {
    if (recursive)
      getChildrenRecursive(categoryId)
        .map(find)
    else
      getChildren(categoryId)
        .map(find)
  }

  private def find(categoryId: Long): Category =
    records.find(_.id == categoryId).get

  private def getChildren(categoryId: Long): Seq[Long] =
    records.filter(_.parentCategoryId.contains(categoryId)).map(_.id)

  private def getChildrenRecursive(id: Long): Seq[Long] = {
    @tailrec
    def helper(q: Seq[Long], acc: Seq[Long] = Seq()): Seq[Long] = {
      if (q.isEmpty) return acc
      val c = getChildren(q.head)
      helper(q.tail ++ c, acc ++ c)
    }
    helper(Seq(id))
  }
}
