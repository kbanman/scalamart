package com.indilago.scalamart

import com.indilago.scalamart.testutil.{RandomHelpers, TestClock}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.duration._

abstract class BaseTestSuite extends FlatSpec
  with BeforeAndAfterEach
  with RandomHelpers
  with MockitoSugar
  with ScalaFutures
  with Matchers
  with TestClock {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 500.millis)
}
