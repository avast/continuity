package com.avast

package object continuity {

  private[continuity] type Context = Map[String, String]

  /** ThreadLocal storage of the Continuity context. */
  private[continuity] val context = new ThreadLocal[Context]() {
    override def initialValue() = Map()
  }

}
