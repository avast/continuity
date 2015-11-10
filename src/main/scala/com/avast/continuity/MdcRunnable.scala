package com.avast.continuity

/** Wraps any Runnable and transfers the [[org.slf4j.MDC]] context
  * from the current thread to the thread this Runnable runs in.
  */
private class MdcRunnable(wrapped: Runnable) extends Runnable with MdcWrapper {

  override def run(): Unit = wrap {
    wrapped.run()
  }

}
