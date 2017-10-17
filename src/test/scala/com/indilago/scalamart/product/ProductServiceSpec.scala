package com.indilago.scalamart.product

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.exception.EntityNotFound
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.{InjectionHelpers, ProductHelpers}
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class ProductServiceSpec extends BaseTestSuite with InjectionHelpers with ProductHelpers {

  val dao: FakeProductDao = productDao
  def sut: ProductService = injector.getInstance(classOf[ProductService])

  "ProductService" should "insert a product" in {
    val product = makeProduct
    val input = makeProductInput(product)

    val created = sut.create(input).futureValue

    created.withoutId shouldEqual product
    notifier.find(ActionType.Create, classOf[BasicProduct]).length shouldBe 1

    verify(dao).create(product)
  }

  it should "gracefully handle a missing product" in {
    sut.find(0).failed.futureValue shouldBe an[EntityNotFound]
  }

  it should "delete a product" in {
    val product = dao.create(makeProduct).futureValue

    sut.delete(product).futureValue shouldBe true
    sut.delete(product).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[BasicProduct]).length shouldBe 1

    verify(dao, times(2)).delete(product.id)
  }
}
