package com.indilago.scalamart.product.option

import com.indilago.scalamart.product.BaseProduct
import com.indilago.scalamart.util.Crud

import scala.concurrent.{ExecutionContext, Future}

trait ProductOptionDao extends Crud[ProductOption, Long] {
  def optionsForProduct(product: BaseProduct)(implicit ec: ExecutionContext): Future[Seq[ProductOption]]

  def findOptionProduct(product: BaseProduct, option: ProductOption)(implicit ec: ExecutionContext): Future[Option[OptionProduct]]

  def addOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int]

  def updateOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[OptionProduct]

  def removeOptionProduct(op: OptionProduct)(implicit ec: ExecutionContext): Future[Int]
}
