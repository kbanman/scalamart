package com.indilago.scalamart.testutil

import java.time.{Clock, Instant, ZoneOffset}

trait TestClock {
  val clock: Clock = Clock.fixed(Instant.now, ZoneOffset.UTC)
}
