package com.indilago.scalamart.models.product

import java.time._

case class BasicProduct(
  id: Long,
  name: String,
  description: String,
  availabilityStart: Option[Instant],
  availabilityEnd: Option[Instant]
)
