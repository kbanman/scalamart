package com.indilago.scalamart.testutil

import java.time.Clock

import com.google.inject.{AbstractModule, Guice, Injector}
import com.indilago.scalamart.category.{CategoryDao, CategoryService, FakeCategoryDao}
import com.indilago.scalamart.order.{FakeOrderDao, OrderDao}
import com.indilago.scalamart.product.configuration.{FakeProductConfigurationDao, ProductConfigurationDao}
import com.indilago.scalamart.product.option._
import com.indilago.scalamart.product.option.price.{FakeOptionPriceDao, OptionPriceDao}
import com.indilago.scalamart.product.{FakeProductDao, ProductDao}
import com.indilago.scalamart.product.price.{FakeProductPriceDao, ProductPriceDao, ProductPriceService}
import com.indilago.scalamart.services.{ActionNotificationService, FakeNotificationService}
import net.codingwell.scalaguice.ScalaModule

trait InjectionHelpers { this: TestClock =>

  val notifier: FakeNotificationService = new FakeNotificationService
  val productDao: FakeProductDao = new FakeProductDao(clock)
  val productPriceDao: FakeProductPriceDao = new FakeProductPriceDao(clock)
  val productConfigDao: FakeProductConfigurationDao = new FakeProductConfigurationDao(clock)
  val optionPriceDao: FakeOptionPriceDao = new FakeOptionPriceDao(clock)
  val orderDao: FakeOrderDao = new FakeOrderDao(clock)
  val categoryDao: FakeCategoryDao = new FakeCategoryDao
  val productOptionDao: FakeProductOptionDao = new FakeProductOptionDao
  val productOptionItemDao: FakeProductOptionItemDao = new FakeProductOptionItemDao

  /**
    * Allow configuration at the test level
    */
  def di: AbstractModule = new AbstractModule {
    def configure(): Unit = {}
  }

  val testModule = new AbstractModule with ScalaModule {
    override def configure() = {
      bind[ProductPriceDao].toInstance(productPriceDao)
      bind[OptionPriceDao].toInstance(optionPriceDao)
      bind[OrderDao].toInstance(orderDao)
      bind[ProductDao].toInstance(productDao)
      bind[CategoryDao].toInstance(categoryDao)
      bind[ProductOptionDao].toInstance(productOptionDao)
      bind[ProductOptionItemDao].toInstance(productOptionItemDao)
      bind[ProductConfigurationDao].toInstance(productConfigDao)
      bind[ActionNotificationService].toInstance(notifier)
      bind[ProductPriceService]
      bind[CategoryService]
      bind[ProductOptionService]
      bind[Clock].toInstance(clock)
    }
  }

  val injector: Injector = Guice.createInjector(testModule, di)
}
