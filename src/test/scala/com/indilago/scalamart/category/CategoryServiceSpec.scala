package com.indilago.scalamart.category

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.exception.EntityNotFound
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class CategoryServiceSpec extends BaseTestSuite with InjectionHelpers with CategoryHelpers {

  val dao: FakeCategoryDao = categoryDao
  def sut: CategoryService = injector.getInstance(classOf[CategoryService])

  "CategoryService" should "insert a category" in {
    val category = makeCategory
    val input = makeCategoryInput(category)

    val created = sut.create(input).futureValue

    created.withoutId shouldEqual category
    notifier.find(ActionType.Create, classOf[Category]).length shouldBe 1

    verify(dao).create(category)
  }

  it should "gracefully handle a missing category" in {
    sut.find(0).failed.futureValue shouldBe an[EntityNotFound]
  }

  it should "find a category by slug" in {
    dao.create(makeCategory).futureValue
    val c2 = dao.create(makeCategory).futureValue

    sut.find(c2.slug).futureValue shouldBe c2
  }

  it should "update a category" in {
    val category = dao.create(makeCategory).futureValue
    val updated = category.copy(name = "Changed")

    sut.update(updated).futureValue shouldBe updated
    sut.find(category.id).futureValue shouldBe updated
    notifier.find(ActionType.Update, classOf[Category]).length shouldBe 1
  }

  it should "delete a category" in {
    val category = dao.create(makeCategory).futureValue

    sut.delete(category).futureValue shouldBe true
    sut.delete(category).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[Category]).length shouldBe 1

    verify(dao, times(2)).delete(category.id)
  }

  it should "add a product to a category" in {
    val cat = dao.create(makeCategory).futureValue
    val p1 = dao.addProduct(makeCategoryProduct(cat.id)).futureValue
    val p2 = makeCategoryProduct(cat.id)

    sut.addProduct(p2).futureValue shouldBe p2
    dao.getProducts(cat.id, recursive = false).futureValue.withoutSort shouldBe Seq(p2, p1)
  }

  it should "get products in a category" in {
    val cat = dao.create(makeCategory).futureValue
    val p1 = dao.addProduct(makeCategoryProduct(cat.id)).futureValue
    dao.addProduct(makeCategoryProduct(positiveLong)).futureValue
    val p3 = dao.addProduct(makeCategoryProduct(cat.id)).futureValue

    sut.getProducts(cat).futureValue.withoutSort shouldBe Seq(p3, p1)
  }

  it should "add a child category" in {
    val parent = dao.create(makeCategory).futureValue
    val child1 = dao.create(makeCategory(parent.id)).futureValue
    val child2 = makeCategory(parent.id)

    sut.create(makeCategoryInput(child2)).futureValue.withoutId shouldBe child2
    dao.getChildren(parent.id, recursive = false).futureValue.withoutId shouldBe Seq(child1, child2).withoutId
  }

  it should "list child categories" in {
    val parent = dao.create(makeCategory).futureValue
    val child1 = dao.create(makeCategory(parent.id)).futureValue
    dao.create(makeCategory(positiveLong)).futureValue
    val child3 = dao.create(makeCategory(parent.id)).futureValue

    sut.getChildren(parent).futureValue shouldBe Seq(child1, child3)
  }

  it should "get recursive products for a category" in {
    val parent = dao.create(makeCategory).futureValue
    val another = dao.create(makeCategory).futureValue
    val c1 = dao.create(makeCategory(parent.id)).futureValue
    dao.create(makeCategory(parent.id)).futureValue
    val c1a = dao.create(makeCategory(c1.id)).futureValue
    val c1b = dao.create(makeCategory(c1.id)).futureValue
    val p1 = dao.addProduct(makeCategoryProduct(parent.id)).futureValue
    dao.addProduct(makeCategoryProduct(another.id)).futureValue
    val p3 = dao.addProduct(makeCategoryProduct(c1.id)).futureValue
    val p4 = dao.addProduct(makeCategoryProduct(c1a.id)).futureValue
    val p5 = dao.addProduct(makeCategoryProduct(c1b.id)).futureValue

    sut.getProducts(parent).futureValue shouldBe Seq(p1)
    sut.getProducts(parent, recursive = true).futureValue.toSet shouldBe Set(
      p1, p3, p4, p5
    )
  }

  it should "reposition products in a category on addition" in {
    val cat = dao.create(makeCategory).futureValue
    val p1 = dao.addProduct(makeCategoryProduct(cat.id)).futureValue
    val p2 = dao.addProduct(makeCategoryProduct(cat.id)).futureValue
    val p3 = dao.addProduct(makeCategoryProduct(cat.id)).futureValue

    sut.getProducts(cat).futureValue shouldBe Seq(
      p3.copy(sort = 0),
      p2.copy(sort = 1),
      p1.copy(sort = 2)
    )
  }
}
