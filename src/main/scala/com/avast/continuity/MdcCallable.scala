package com.avast.continuity

import java.util.concurrent.Callable

/** Wraps any Callable and transfers the [[org.slf4j.MDC]] context
  * from the current thread to the thread this Callable runs in.
  */
private class MdcCallable[A](wrapped: Callable[A])(implicit threadNamer: ThreadNamer) extends Callable[A] with MdcWrapper {

  override def call(): A = wrap {
    wrapped.call()
  }

}
