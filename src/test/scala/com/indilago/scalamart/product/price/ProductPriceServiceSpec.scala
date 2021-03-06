package com.indilago.scalamart.product.price

import java.util.Currency

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers

import scala.concurrent.ExecutionContext.Implicits.global

class ProductPriceServiceSpec extends BaseTestSuite with InjectionHelpers with PriceHelpers {

  val dao: FakeProductPriceDao = productPriceDao
  def sut: ProductPriceService = injector.getInstance(classOf[ProductPriceService])

  override def beforeEach(): Unit = {
    super.beforeEach()
    notifier.reset()
  }

  "ProductPriceService" should "insert a product price" in {
    val price = makePrice()
    val input = makePriceInput(price)

    val created = sut.createPrice(input).futureValue

    created.withoutId shouldEqual price
    notifier.find(ActionType.Create, classOf[ProductPrice]).length shouldBe 1
  }

  it should "get the current price for a product" in {
    val price = makePrice().insert()
    sut.currentPrice(price.productId, price.currency).futureValue shouldBe price
  }

  it should "gracefully handle missing prices" in {
    sut.currentPrice(1, Currency.getInstance("AUD")).failed.futureValue shouldBe a[NoProductPriceError]
  }

  it should "delete a price" in {
    val price = makePrice().insert()

    sut.delete(price).futureValue shouldBe true
    sut.delete(price).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[ProductPrice]).length shouldBe 1
  }
}
