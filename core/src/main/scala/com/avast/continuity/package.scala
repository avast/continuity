package com.avast

package object continuity {

  private[continuity] val context = new ThreadLocal[Map[String, String]]() {
    override def initialValue() = Map()
  }

}
