package com.avast.continuity

object IdentityThreadNamer extends ThreadNamer {

  override def nameThread[A](block: => A): A = block

  override protected def name: String = Thread.currentThread.getName

}
