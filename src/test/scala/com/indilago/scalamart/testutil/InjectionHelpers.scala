package com.indilago.scalamart.testutil

import java.time.Clock

import com.google.inject.{AbstractModule, Guice, Injector}
import com.indilago.scalamart.category.{CategoryDao, CategoryService, FakeCategoryDao}
import com.indilago.scalamart.product.{FakeProductDao, ProductDao}
import com.indilago.scalamart.product.price.{FakeProductPriceDao, ProductPriceDao, ProductPriceService}
import com.indilago.scalamart.services.{ActionNotificationService, FakeNotificationService}
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Mockito.spy

trait InjectionHelpers { this: TestClock =>

  val notifier: FakeNotificationService = spy(new FakeNotificationService)
  val productDao: FakeProductDao = spy(new FakeProductDao(clock))
  val productPriceDao: FakeProductPriceDao = spy(new FakeProductPriceDao(clock))
  val categoryDao: FakeCategoryDao = spy(new FakeCategoryDao)

  /**
    * Allow configuration at the test level
    */
  def di: AbstractModule = new AbstractModule {
    def configure(): Unit = {}
  }

  val testModule = new AbstractModule with ScalaModule {
    override def configure() = {
      bind[ProductPriceDao].toInstance(productPriceDao)
      bind[ProductDao].toInstance(productDao)
      bind[CategoryDao].toInstance(categoryDao)
      bind[Clock].toInstance(clock)
      bind[ActionNotificationService].toInstance(notifier)
      bind[ProductPriceService]
      bind[CategoryService]
    }
  }

  val injector: Injector = Guice.createInjector(testModule, di)
}
