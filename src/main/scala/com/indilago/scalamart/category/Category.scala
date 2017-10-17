package com.indilago.scalamart.category

import com.indilago.scalamart.Identifiable

case class Category(
  id: Long,
  slug: String,
  parentCategoryId: Option[Long],
  name: String,
  description: String
) extends Identifiable

case class CategoryInput(
  slug: String,
  parentCategoryId: Option[Long],
  name: String,
  description: String,
)