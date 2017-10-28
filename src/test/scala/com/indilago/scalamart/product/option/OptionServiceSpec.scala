package com.indilago.scalamart.product.option

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.exception.{BadInput, EntityNotFound, PreconditionFailed}
import com.indilago.scalamart.product.ProductHelpers
import com.indilago.scalamart.product.option.ProductOptionType._
import com.indilago.scalamart.services.ActionType.{Create, Delete, Update}
import com.indilago.scalamart.testutil.InjectionHelpers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global

class OptionServiceSpec extends BaseTestSuite with BeforeAndAfterEach with InjectionHelpers with OptionHelpers with ProductHelpers {

  val dao: FakeProductOptionDao = productOptionDao
  val itemDao: FakeProductOptionItemDao = productOptionItemDao
  def sut: ProductOptionService = injector.getInstance(classOf[ProductOptionService])

  override def beforeEach(): Unit = {
    super.beforeEach()
    notifier.reset()
  }

  "ProductOptionService" should "create an option" in {
    val option = makeOption(Basic)
    val input = makeOptionInput(option)

    val created = sut.create(input).futureValue

    created.withoutId shouldEqual option
    notifier.find(Create, classOf[ProductOption]).length shouldBe 1

    verify(dao).create(option)
  }

  it should "find an option" in {
    val option = insertOption()

    sut.require(option.id).futureValue shouldBe option
  }

  it should "throw a NotFound error" in {
    sut.require(positiveLong).failed.futureValue shouldBe a[EntityNotFound]
  }

  it should "return an optional when searching by id" in {
    val option = insertOption()

    sut.find(option.id).futureValue shouldBe Some(option)
    sut.find(positiveLong).futureValue shouldBe None
  }

  it should "update an option" in {
    val option = insertOption(Product)
    val item = insertItem(option)
    val updated = option.copy(defaultItemId = Some(item.id), name = "Changed")

    sut.update(updated).futureValue shouldBe updated
    notifier.find(Update, classOf[ProductOption]).length shouldBe 1
  }

  it should "not allow updating to a nonexistent default item" in {
    val option = insertOption(Basic)
    val updated = option.copy(defaultItemId = Some(positiveLong))

    sut.update(updated).failed.futureValue shouldBe a[BadInput]
    notifier.find(Update, classOf[ProductOption]).length shouldBe 0
  }

  it should "not allow updating option type if child items exist" in {
    val option = insertOption(Basic)
    insertItem(option)
    val updated = option.copy(kind = Product)

    sut.update(updated).failed.futureValue shouldBe a[PreconditionFailed]
    notifier.find(Update, classOf[ProductOption]).length shouldBe 0
  }

  it should "delete an option" in {
    val option = insertOption(Basic)

    sut.delete(option).futureValue shouldBe true
    sut.delete(option).futureValue shouldBe false
    notifier.find(Delete, classOf[ProductOption]).length shouldBe 1
  }

  it should "delete items when deleting option" in {
    val option = insertOption(Basic)
    insertItem(option)

    sut.delete(option).futureValue shouldBe true
    notifier.find(Delete, classOf[ProductOption]).length shouldBe 1
    notifier.find(Delete, classOf[OptionItem]).length shouldBe 1
  }

  it should "get items for an option" in {
    val option = insertOption(Product)
    val i1 = insertItem(option)
    val i2 = insertItem(option)

    sut.getItems(option).futureValue shouldBe Seq(i1, i2)
  }

  it should "create a basic item" in {
    val option = insertOption(Basic)
    val item = makeItem(option)

    sut.createItem(makeItemInput(item)).futureValue.withoutId shouldBe item
    notifier.find(Create, classOf[OptionItem]).length shouldBe 1
  }

  it should "create a product item" in {
    val option = insertOption(Product)
    val item = makeItem(option)

    sut.createItem(makeItemInput(item)).futureValue.withoutId shouldBe item
    notifier.find(Create, classOf[OptionItem]).length shouldBe 1
  }

  it should "not allow creating an item for nonexistent product" in {
    val option = makeOption(Product).copy(id = positiveLong)
    val item = makeItem(option)

    sut.createItem(makeItemInput(item)).failed.futureValue shouldBe a[BadInput]
    notifier.find(Create, classOf[OptionItem]).length shouldBe 0
  }

  it should "not allow creating an item with wrong type" in {
    val option = insertOption(Product)
    val option2 = option.copy(kind = Basic)
    val item = makeItem(option2)

    sut.createItem(makeItemInput(item)).failed.futureValue shouldBe a[BadInput]
    notifier.find(Create, classOf[OptionItem]).length shouldBe 0
  }

  it should "idempotently attach an option to a product" in {
    val option = insertOption(Basic)
    val product = makeProduct.copy(id = positiveLong)
    val op = OptionProduct(option.id, product.id, min = 0, max = 1)

    sut.addOption(op).futureValue shouldBe true
    sut.addOption(op).futureValue shouldBe false
    notifier.find(Create, classOf[OptionProduct]).length shouldBe 1
  }

  it should "idempotently remove an option from a product" in {
    val option = insertOption(Basic)
    val product = makeProduct
    val op = OptionProduct(option.id, product.id, min = 1, max = 1)
    sut.addOption(op).futureValue

    sut.removeOption(op).futureValue shouldBe true
    sut.removeOption(op).futureValue shouldBe false
    notifier.find(Delete, classOf[OptionProduct]).length shouldBe 1
  }

  it should "get options for a product" in {
    val o1 = insertOption(Basic)
    val o2 = insertOption(Product)
    val product = makeProduct.copy(id = positiveLong)
    val op1 = OptionProduct(o1.id, product.id, min = 1, max = 1)
    val op2 = OptionProduct(o2.id, product.id, min = 1, max = 1)

    sut.getOptions(product).futureValue shouldBe Seq[ProductOption]()
    sut.addOption(op1).futureValue
    sut.addOption(op2).futureValue
    sut.getOptions(product).futureValue shouldBe Seq(o1, o2)
  }

  it should "update a basic option item" in {
    val option = insertOption(Basic)
    val item = insertItem(option).asInstanceOf[BasicOptionItem]
    val updated = item.copy(name = "changed")

    sut.updateItem(updated).futureValue shouldBe updated
    notifier.find(Update, classOf[OptionItem]).length shouldBe 1
  }

  it should "update a product option item" in {
    val option = insertOption(Product)
    val item = insertItem(option).asInstanceOf[ProductOptionItem]
    val updated = item.copy(productId = positiveLong)

    sut.updateItem(updated).futureValue shouldBe updated
    notifier.find(Update, classOf[OptionItem]).length shouldBe 1
  }

  it should "not allow changing an item's parent option" in {
    val option = insertOption(Basic)
    val item = insertItem(option).asInstanceOf[BasicOptionItem]
    val updated = item.copy(optionId = positiveLong)

    sut.updateItem(updated).failed.futureValue shouldBe a[BadInput]
    notifier.find(Update, classOf[OptionItem]).length shouldBe 0
  }

  it should "not allow changing type when updating item" in {
    val option = insertOption(Basic)
    val option2 = makeOption(Product).copy(id = option.id)
    val item = insertItem(option).asInstanceOf[BasicOptionItem]
    val updated = makeItem(option2).asInstanceOf[ProductOptionItem].copy(id = item.id)

    sut.updateItem(updated).failed.futureValue shouldBe a[BadInput]
    notifier.find(Update, classOf[OptionItem]).length shouldBe 0
  }

  it should "delete an item" in {
    val option = insertOption(Basic)
    val item = insertItem(option)

    sut.deleteItem(item).futureValue shouldBe true
    sut.deleteItem(item).futureValue shouldBe false
    notifier.find(Delete, classOf[OptionItem]).length shouldBe 1
  }

  private def insertOption(kind: ProductOptionType = Basic): ProductOption =
    dao.create(makeOption(kind)).futureValue

  private def insertItem(option: ProductOption): OptionItem =
    itemDao.create(makeItem(option)).map { r =>
      option.kind match {
        case Basic =>
          BasicOptionItem(r.id, r.optionId, r.name.get)
        case Product =>
          ProductOptionItem(r.id, r.optionId, r.productId.get)
      }
    }.futureValue
}
