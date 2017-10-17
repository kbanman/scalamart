package com.indilago.scalamart.exception

class BadInput(message: String) extends RuntimeException(message)
object BadInput {
  def apply(message: String): BadInput =
    new BadInput(message)
}
