package com.indilago.scalamart.product

import com.indilago.scalamart.util.Crud

trait ProductDao extends Crud[BaseProduct, Long]
