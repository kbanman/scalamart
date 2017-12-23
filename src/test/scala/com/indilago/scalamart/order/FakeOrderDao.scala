package com.indilago.scalamart.order

import java.time.Clock

import com.indilago.scalamart.testutil.FakeCrud

class FakeOrderDao(clock: Clock) extends OrderDao with FakeCrud[Order] {

  override protected def withId(entity: Order, id: Long): Order =
    entity.copy(id = id)
}
