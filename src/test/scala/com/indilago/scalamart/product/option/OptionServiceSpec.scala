package com.indilago.scalamart.product.option

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.product.option.ProductOptionType._
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class OptionServiceSpec extends BaseTestSuite with InjectionHelpers with OptionHelpers {

  val dao: FakeProductOptionDao = productOptionDao
  def sut: ProductOptionService = injector.getInstance(classOf[ProductOptionService])

  "ProductPriceService" should "insert an option" in {
    val option = makeOption(Basic)
    val input = makeOptionInput(option)

    val created = sut.create(input).futureValue

    created.withoutId shouldEqual option
    notifier.find(ActionType.Create, classOf[ProductOption]).length shouldBe 1

    verify(dao).create(option)
  }
}
