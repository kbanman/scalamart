package com.indilago.scalamart.product.option.price

import java.util.Currency

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers

import scala.concurrent.ExecutionContext.Implicits.global

class OptionPriceServiceSpec extends BaseTestSuite with InjectionHelpers with PriceHelpers {

  val dao: FakeOptionPriceDao = optionPriceDao
  def sut: OptionPriceService = injector.getInstance(classOf[OptionPriceService])

  "OptionPriceService" should "insert an option item price" in {
    val price = makePrice()
    val input = makePriceInput(price)

    val created = sut.createPrice(input).futureValue

    created.withoutId shouldEqual price
    notifier.find(ActionType.Create, classOf[OptionPrice]).length shouldBe 1
  }

  it should "get the current price for an option" in {
    val price = dao.create(makePrice()).futureValue
    sut.currentPrice(price.itemId, price.currency).futureValue shouldBe price
  }

  it should "gracefully handle missing prices" in {
    sut.currentPrice(1, Currency.getInstance("AUD")).failed.futureValue shouldBe a[NoOptionPriceError]
  }

  it should "delete a price" in {
    val price = dao.create(makePrice()).futureValue

    sut.delete(price).futureValue shouldBe true
    sut.delete(price).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[OptionPrice]).length shouldBe 1
  }
}
