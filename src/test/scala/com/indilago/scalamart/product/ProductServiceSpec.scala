package com.indilago.scalamart.product

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.exception.EntityNotFound
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class ProductServiceSpec extends BaseTestSuite with InjectionHelpers with ProductHelpers {

  val dao: FakeProductDao = productDao
  def sut: ProductService = injector.getInstance(classOf[ProductService])

  override def beforeEach(): Unit = {
    super.beforeEach()
    notifier.reset()
  }

  "ProductService" should "insert a product" in {
    val product = makeProduct
    val input = makeProductInput(product)

    val created = sut.create(input).futureValue

    created.withoutId shouldEqual product
    notifier.find(ActionType.Create, classOf[BaseProduct]).length shouldBe 1
  }

  it should "gracefully handle a missing product" in {
    sut.require(0).failed.futureValue shouldBe an[EntityNotFound]
  }

  it should "update a product" in {
    val product = dao.create(makeProduct).futureValue
    val updated = product.copy(name = "Changed")

    sut.update(updated).futureValue shouldBe updated
    sut.require(product.id).futureValue shouldBe updated
    notifier.find(ActionType.Update, classOf[BaseProduct]).length shouldBe 1
  }

  it should "delete a product" in {
    val product = dao.create(makeProduct).futureValue

    sut.delete(product).futureValue shouldBe true
    sut.delete(product).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[BaseProduct]).length shouldBe 1
  }
}
