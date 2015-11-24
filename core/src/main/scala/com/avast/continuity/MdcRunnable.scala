package com.avast.continuity

/** Wraps any Runnable and transfers the context and [[org.slf4j.MDC]]
  * from the current thread to the thread this Runnable runs in.
  */
private class MdcRunnable(wrapped: Runnable)(implicit threadNamer: ThreadNamer) extends Runnable with MdcWrapper {

  override def run(): Unit = wrap {
    wrapped.run()
  }

}
