package com.indilago.scalamart.order

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.exception.EntityNotFound
import com.indilago.scalamart.product.configuration.ProductConfigHelpers
import com.indilago.scalamart.product.option.OptionHelpers
import com.indilago.scalamart.product.price.PriceHelpers
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers

import scala.concurrent.ExecutionContext.Implicits.global

class OrderServiceSpec extends BaseTestSuite
  with InjectionHelpers with OrderHelpers with ProductConfigHelpers with PriceHelpers with OptionHelpers {

  val dao: FakeOrderDao = orderDao
  def sut: OrderService = injector.getInstance(classOf[OrderService])

  override def beforeEach(): Unit = {
    super.beforeEach()
    notifier.reset()
  }

  "OrderService" should "create an order" in {
    val order = makeOrder

    sut.create(order).futureValue.withoutId shouldEqual order

    notifier.find(ActionType.Create, classOf[Order]).length shouldBe 1
  }

  it should "create an order with items" in {
    // Item1
    val config1 = insertConfig
    val price1 = insertPrice(config1.productId)
    val item1 = makeOrderItem(config1, price1, Map())

    // Item2
    val config2 = insertConfig
    val price2 = insertPrice(config2.productId)
    val option2_1 = makeOption
    val optionItem2_1 = makeOptionItem(option2_1)
    val option2_2 = makeOption
    val optionItem2_2 = makeOptionItem(option2_2)
    val item2 = makeOrderItem
    val order = makeOrder(Seq(item1, item2))

    sut.create(order).futureValue.withoutId shouldEqual order
    notifier.find(ActionType.Create, classOf[Order]).length shouldBe 1
  }

  it should "gracefully handle a missing order" in {
    sut.find(0).failed.futureValue shouldBe an[EntityNotFound]
  }

  it should "delete an order" in {
    val order = dao.create(makeOrder).futureValue

    sut.delete(order).futureValue shouldBe true
    sut.delete(order).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[Order]).length shouldBe 1
  }
}
