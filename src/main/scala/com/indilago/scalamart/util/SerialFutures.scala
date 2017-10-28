package com.indilago.scalamart.util

import scala.concurrent.{ExecutionContext, Future}

object SerialFutures {
  implicit class SerialFutureSeq[T](list: Seq[T]) {
    def serialMap[B](fn: T => Future[B])(implicit ec: ExecutionContext): Future[Seq[B]] =
      list match {
        case Seq() =>
          Future.successful(Seq())
        case Seq(el) =>
          fn(el).map(res => Seq(res))
        case _ =>
          list.tail.foldLeft(wrap(fn(list.head))(Seq())) { (f, el) =>
            f.flatMap(acc => fn(el).map(res => acc :+ res))
          }
      }

    private def wrap[B](f: Future[B])(acc: Seq[B])(implicit ec: ExecutionContext): Future[Seq[B]] =
      f.map(res => acc :+ res)
  }
}
