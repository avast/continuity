package com.avast.continuity

/** Does not rename a thread, keeps its original name. */
object IdentityThreadNamer extends ThreadNamer {

  override def nameThread[A](block: => A): A = block

  override protected def name: String = Thread.currentThread.getName

}
