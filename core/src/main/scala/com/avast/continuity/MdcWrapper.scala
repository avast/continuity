package com.avast.continuity

import org.slf4j.MDC

private trait MdcWrapper {

  /** Stores the current value of the context when this is instantiated. */
  protected val continuationContext: Context = context.get

  protected def wrap[A](block: => A)(implicit threadNamer: ThreadNamer): A = {
    val originalContext = context.get
    try {
      context.set(continuationContext)
      continuationContext.foreach { case (key, value) =>
        MDC.put(key, value)
      }

      threadNamer.nameThread {
        block
      }
    } finally {
      context.set(originalContext)
      continuationContext.foreach { case (key, _) =>
        MDC.remove(key)
      }
    }
  }

}
