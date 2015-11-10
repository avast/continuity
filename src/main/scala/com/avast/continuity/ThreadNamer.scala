package com.avast.continuity

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
