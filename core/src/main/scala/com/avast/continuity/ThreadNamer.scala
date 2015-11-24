package com.avast.continuity

/** Temporarily renames a thread for the given block of code. */
trait ThreadNamer {

  def nameThread[A](block: => A): A = {
    val original = Thread.currentThread.getName
    try {
      Thread.currentThread.setName(name)
      block
    } finally {
      Thread.currentThread.setName(original)
    }
  }

  protected def name: String

}
