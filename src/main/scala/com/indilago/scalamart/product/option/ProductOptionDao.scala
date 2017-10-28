package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.BaseProduct

import scala.concurrent.{ExecutionContext, Future}

trait ProductOptionDao {
  def optionsForProduct(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]]

  def findOptionProduct(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Option[OptionProduct]]

  def addOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int]

  def updateOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[OptionProduct]

  def removeOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int]

  def find(optionId: Long)(implicit ec: ExecutionContext): Future[Option[ProductOption]]

  def create(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption]

  def update(option: ProductOption)(implicit ec: ExecutionContext): Future[ProductOption]

  def delete(option: ProductOption)(implicit ec: ExecutionContext): Future[Int]
}
