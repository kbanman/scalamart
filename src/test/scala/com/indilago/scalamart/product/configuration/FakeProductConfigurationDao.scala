package com.indilago.scalamart.product.configuration

import java.time.Clock

import com.indilago.scalamart.testutil.FakeCrud

class FakeProductConfigurationDao(protected val clock: Clock) extends ProductConfigurationDao with FakeCrud[ProductConfig] {

  override protected def withId(entity: ProductConfig, id: Long): ProductConfig =
    entity.copy(id = id)
}
