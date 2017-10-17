package com.indilago.scalamart.product

import java.time._

import com.indilago.scalamart.Identifiable

case class BasicProduct(
  id: Long,
  created: Instant,
  name: String,
  description: String,
  availabilityStart: Option[Instant],
  availabilityEnd: Option[Instant]
) extends Identifiable

case class ProductInput(
  name: String,
  description: String,
  availabilityStart: Option[Instant],
  availabilityEnd: Option[Instant]
)
