package com.indilago.scalamart.exception

class EntityNotFound(message: String) extends RuntimeException(message)

object EntityNotFound {
  def apply(entity: Class[_], id: Any): EntityNotFound =
    new EntityNotFound(s"Entity $entity not found for id $id")
}
