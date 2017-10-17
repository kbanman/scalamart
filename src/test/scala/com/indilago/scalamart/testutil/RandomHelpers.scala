package com.indilago.scalamart.testutil

trait RandomHelpers {

  private val r = scala.util.Random

  def positiveInt(max: Int): Int =
    r.nextInt(max)

  def positiveInt: Int =
    positiveInt(1000000)

  def positiveLong: Long =
    Math.abs(r.nextLong)

  def positiveDecimal(max: Int): BigDecimal =
    BigDecimal(positiveInt(max) + r.nextInt(99).toDouble/100)

  def positiveDecimal: BigDecimal =
    positiveDecimal(1000000)

  def alphaNum(length: Int): String =
    r.alphanumeric.take(length).mkString("")

  def words(num: Int, maxWordLength: Int = 8): String =
    (1 to num).foldLeft(Seq[String]()) { (words, _) =>
      words :+ alphaNum(positiveInt(maxWordLength))
    }.mkString(" ")
}
