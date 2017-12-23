package com.indilago.scalamart.order

import com.indilago.scalamart.util.Crud

trait OrderDao extends Crud[Order, Long]
