package com.avast.continuity

import java.util.concurrent.{Executor, ExecutorService, ScheduledExecutorService}

import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}

/* KEEP THIS IMPLEMENTATION IN SYNC WITH THE JAVA API VERSION */

trait Continuity {

  protected[this] def withContext[A](ctxValues: (String, String)*)(block: => A)(
      implicit threadNamer: ThreadNamer = IdentityThreadNamer): A = {
    Continuity.withContext(ctxValues: _*)(block)
  }

  protected[this] def getFromContext(key: String): Option[String] = Continuity.getFromContext(key)

  protected[this] def putToContext(key: String, value: String): Unit = Continuity.putToContext(key, value)

  protected[this] def removeFromContext(key: String): Unit = Continuity.removeFromContext(key)

}

/** Provides methods to work with the Continuity context and factory methods to wrap thread pools. */
object Continuity {

  /** Marks that the current thread was already processed. */
  private final val Marker = "__MARKER__"

  /** Creates an instance of [[com.avast.continuity.Continuity]] in case you don't want to use the static methods. */
  def apply(implicit threadNamer: ThreadNamer = IdentityThreadNamer): Continuity = new Continuity {}

  /** Puts the given values into the context, names a thread and runs the given block of code.
    * It correctly cleans up everything after the block finishes.
    *
    * This method is to be used at the leaves of Continuity context usage meaning that you have to fill in the context
    * somewhere and this method should be used for that. From there the context is propagated automatically.
    */
  def withContext[A](ctxValues: (String, String)*)(block: => A)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): A = {
    if (getFromContext(Marker).isEmpty) {
      try {
        putToContext(Marker, "")
        ctxValues.foreach {
          case (key, value) =>
            putToContext(key, value)
        }

        threadNamer.nameThread {
          block
        }
      } finally {
        ctxValues.foreach {
          case (key, _) =>
            removeFromContext(key)
        }
        removeFromContext(Marker)
      }
    } else {
      block
    }
  }

  def getFromContext(key: String): Option[String] = context.get.get(key)

  def putToContext(key: String, value: String): Unit = {
    val ctx = context.get
    context.set(ctx + (key -> value))
    MDC.put(key, value)
  }

  def removeFromContext(key: String): Unit = {
    val ctx = context.get
    context.set(ctx - key)
    MDC.remove(key)
  }

  def wrapExecutionContext(executor: ExecutionContext)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContext =
    preventDoubleWrap(executor) {
      new ContinuityExecutionContext(executor)(threadNamer)
    }

  def wrapExecutionContextExecutor(executor: ExecutionContextExecutor)(
      implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContextExecutor = preventDoubleWrap(executor) {
    new ContinuityExecutionContextExecutor(executor)(threadNamer)
  }

  def wrapExecutionContextExecutorService(executor: ExecutionContextExecutorService)(
      implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContextExecutorService = {
    preventDoubleWrap(executor) {
      new ContinuityExecutionContextExecutorService(executor)(threadNamer)
    }
  }

  def wrapExecutor(executor: Executor)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): Executor = preventDoubleWrap(executor) {
    new ContinuityExecutor(executor)(threadNamer)
  }

  def wrapExecutorService(executor: ExecutorService)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutorService =
    preventDoubleWrap(executor) {
      new ContinuityExecutorService(executor)(threadNamer)
    }

  def wrapScheduledExecutorService(executor: ScheduledExecutorService)(
      implicit threadNamer: ThreadNamer = IdentityThreadNamer): ScheduledExecutorService =
    preventDoubleWrap(executor) {
      new ContinuityScheduledExecutorService(executor)(threadNamer)
    }

  private def preventDoubleWrap[A](executor: A)(wrap: => A): A = executor match {
    case _: ContinuityExecutorMarker => executor
    case _ => wrap
  }

}
