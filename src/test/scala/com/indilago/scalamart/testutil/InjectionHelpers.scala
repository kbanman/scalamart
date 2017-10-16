package com.indilago.scalamart.testutil

import java.time.Clock

import com.google.inject.{AbstractModule, Guice, Injector}
import com.indilago.scalamart.dao.{FakeProductPriceDao, ProductPriceDao}
import com.indilago.scalamart.services.{ActionNotificationService, FakeNotificationService, ProductPriceService}
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Mockito.spy

trait InjectionHelpers { this: TestClock =>

  val notifier: FakeNotificationService = spy(new FakeNotificationService)
  val productPriceDao: FakeProductPriceDao = spy(new FakeProductPriceDao(clock))

  /**
    * Allow configuration at the test level
    */
  def di: AbstractModule = new AbstractModule {
    def configure(): Unit = {}
  }

  val testModule = new AbstractModule with ScalaModule {
    override def configure() = {
      bind[ProductPriceDao].toInstance(productPriceDao)
      bind[Clock].toInstance(clock)
      bind[ActionNotificationService].toInstance(notifier)
      bind[ProductPriceService]
    }
  }

  val injector: Injector = Guice.createInjector(testModule, di)
}
