package com.indilago.scalamart.category

import com.indilago.scalamart.testutil.{RandomHelpers, TestClock}

trait CategoryHelpers {
  this: RandomHelpers with TestClock =>

  def makeCategory(parentId: Option[Long]) =
    Category(
      id = 0,
      slug = s"cat-${alphaNum(4)}",
      name = s"Category ${alphaNum(6)}",
      description = s"Description ${words(10)}",
      parentCategoryId = parentId
    )

  def makeCategory: Category =
    makeCategory(None)

  def makeCategory(parentId: Long): Category =
    makeCategory(Some(parentId))

  def makeCategoryInput(category: Category): CategoryInput =
    CategoryInput(
      name = category.name,
      slug = category.slug,
      parentCategoryId = category.parentCategoryId,
      description = category.description,
    )

  def makeCategoryProduct(categoryId: Long, sort: Int = 0): CategoryProduct =
    CategoryProduct(
      categoryId = categoryId,
      productId = positiveLong,
      sort = sort
    )

  implicit class CategoryHelpers(c: Category) {
    def withoutId: Category =
      c.copy(id = 0)
  }

  implicit class CategoryListHelpers(l: Seq[Category]) {
    def withoutId: Seq[Category] =
      l.map(_.copy(id = 0))
  }

  implicit class CategoryProductHelpers(p: CategoryProduct) {
    def withoutSort: CategoryProduct =
      p.copy(sort = 0)
  }

  implicit class CategoryProductListHelpers(l: Seq[CategoryProduct]) {
    def withoutSort: Seq[CategoryProduct] =
      l.map(_.copy(sort = 0))
  }
}
