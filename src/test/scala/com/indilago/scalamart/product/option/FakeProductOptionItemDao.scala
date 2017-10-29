package com.indilago.scalamart.product.option

import com.indilago.scalamart.testutil.FakeCrud

import scala.concurrent.{ExecutionContext, Future}

class FakeProductOptionItemDao extends ProductOptionItemDao with FakeCrud[OptionItemRecord] {

  override protected def withId(item: OptionItemRecord, id: Long): OptionItemRecord =
    item.copy(id = id)

  def itemsForOption(option: ProductOption)(implicit ec: ExecutionContext): Future[Seq[OptionItemRecord]] = Future {
    records.filter(_.optionId == option.id)
  }
}
