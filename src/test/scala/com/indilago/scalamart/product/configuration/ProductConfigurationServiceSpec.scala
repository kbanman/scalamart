package com.indilago.scalamart.product.configuration

import com.indilago.scalamart.BaseTestSuite
import com.indilago.scalamart.exception.ValidationFailed
import com.indilago.scalamart.product.option._
import com.indilago.scalamart.product.{BaseProduct, ProductHelpers}
import com.indilago.scalamart.services.ActionType
import com.indilago.scalamart.testutil.InjectionHelpers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProductConfigurationServiceSpec extends BaseTestSuite
  with InjectionHelpers with ProductHelpers with ProductConfigHelpers with OptionHelpers {

  val dao: FakeProductConfigurationDao = productConfigDao
  def sut: ProductConfigurationService = injector.getInstance(classOf[ProductConfigurationService])

  override def beforeEach(): Unit = {
    super.beforeEach()
    notifier.reset()
  }

  "ProductConfigurationService" should "create a configuration" in {
    val product = makeProduct.insert()
    val config = makeConfig(product.id)

    val created = sut.create(config).futureValue

    created.withoutId shouldEqual config
    notifier.find(ActionType.Create, classOf[ProductConfig]).length shouldBe 1
  }

  it should "create a config with option items" in {
    val product = makeProduct.insert()
    val option = makeOption(product, min = 0, max = 2)
    val item1 = insertItem(option)
    val item2 = insertItem(option)

    val config = ProductConfig(
      id = 0,
      productId = product.id,
      options = Map(option.id -> Seq(item1.id -> 1, item2.id -> 1)),
      attributes = Map.empty
    )
    val created = sut.create(config).futureValue
    created shouldBe config.copy(id = created.id)
    productConfigDao.find(created.id).futureValue.get shouldBe created
    notifier.find(ActionType.Create, classOf[ProductConfig]).length shouldBe 1
  }

  it should "validate quantity of option items" in {
    val product = insertProduct
    val option = insertOption(product, min = 0, max = 2)
    val item1 = insertItem(option)
    val item2 = insertItem(option)

    val config = ProductConfig(
      id = 0,
      productId = product.id,
      options = Map(option.id -> Seq(item1.id -> 1, item2.id -> 2)),
      attributes = Map.empty
    )
    sut.create(config).failed.futureValue shouldBe a[ValidationFailed]
    notifier.find(ActionType.Create, classOf[ProductConfig]).length shouldBe 0
  }

  it should "find a configuration" in {
    val config = insertConfig

    sut.find(config.id).futureValue shouldBe Some(config)
  }

  it should "gracefully handle a missing config" in {
    sut.find(0).futureValue shouldBe None
  }

  it should "update a config" in {
    val config = insertConfig
    val product2 = insertProduct
    val updated = config.copy(productId = product2.id)

    sut.update(updated).futureValue shouldBe updated
    sut.find(config.id).futureValue.get shouldBe updated
    notifier.find(ActionType.Update, classOf[ProductConfig]).length shouldBe 1
  }

  it should "validate quantity when updating" in {
    val product = insertProduct
    val option = insertOption(product, min = 0, max = 2)
    val item1 = insertItem(option)
    val item2 = insertItem(option)
    val config = insertConfig(product, Map(option.id -> Seq(item1.id -> 1, item2.id -> 1)))
    val updated = config.copy(options = Map(option.id -> Seq(item1.id -> 2, item2.id -> 1)))

    sut.update(updated).failed.futureValue shouldBe a[ValidationFailed]
    notifier.find(ActionType.Update, classOf[ProductConfig]).length shouldBe 0
  }

  it should "delete a config" in {
    val product = insertProduct
    val option = insertOption(product, min = 0, max = 2)
    val item1 = insertItem(option)
    val item2 = insertItem(option)
    val config = insertConfig(product, Map(option.id -> Seq(item1.id -> 1, item2.id -> 1)))

    sut.delete(config).futureValue shouldBe true
    sut.delete(config).futureValue shouldBe false
    notifier.find(ActionType.Delete, classOf[ProductConfig]).length shouldBe 1
  }

  private def insertItem(option: ProductOption): OptionItemRecord =
    productOptionItemDao.create(makeOptionItem(option)).futureValue

  private def insertOptionF(product: BaseProduct, min: Int, max: Int): Future[ProductOption] =
    for {
      option <- productOptionDao.create(makeOption(ProductOptionType.Basic))
      _op <- productOptionDao.addOptionProduct(OptionProduct(option.id, product.id, min, max))
    } yield option

  private def insertOption: ProductOption =
    productOptionDao.create(makeOption(ProductOptionType.Basic)).futureValue

  private def insertOption(product: BaseProduct, min: Int, max: Int): ProductOption =
    insertOptionF(product, min, max).futureValue

  private def insertProductF: Future[BaseProduct] =
    productDao.create(makeProduct)

  private def insertProduct: BaseProduct =
    insertProductF.futureValue

  def insertConfigF: Future[ProductConfig] =
    for {
      product <- insertProductF
      config <- insertConfigF(makeConfig(product.id))
    } yield config

}
