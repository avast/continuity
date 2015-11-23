package com.avast.continuity

import java.util.concurrent.{Executor, ExecutorService}

import com.avast.utils2.concurrent.CompletableFutureExecutorService
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}

trait Continuity {

  protected[this] def withContext[A](ctxValues: (String, String)*)(block: => A)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): A = {
    Continuity.withContext(ctxValues: _*)(block)
  }

  protected[this] def getFromContext(key: String): Option[String] = Continuity.getFromContext(key)

  protected[this] def putToContext(key: String, value: String): Unit = Continuity.putToContext(key, value)

  protected[this] def removeFromContext(key: String): Unit = Continuity.removeFromContext(key)

}

object Continuity {

  def apply(implicit threadNamer: ThreadNamer = IdentityThreadNamer): Continuity = new Continuity {}

  def withContext[A](ctxValues: (String, String)*)(block: => A)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): A = try {
    ctxValues.foreach { case (key, value) =>
      putToContext(key, value)
    }

    threadNamer.nameThread {
      block
    }
  } finally {
    ctxValues.foreach { case (key, _) =>
      removeFromContext(key)
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

  def wrapExecutionContext(executor: ExecutionContext)
                          (implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContext = {
    new ContinuityExecutionContext(executor)(threadNamer)
  }

  def wrapExecutionContextExecutor(executor: ExecutionContextExecutor)
                                  (implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContextExecutor = {
    new ContinuityExecutionContextExecutor(executor)(threadNamer)
  }

  def wrapExecutionContextExecutorService(executor: ExecutionContextExecutorService)
                                         (implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContextExecutorService = {
    new ContinuityExecutionContextExecutorService(executor)(threadNamer)
  }

  def wrapExecutor(executor: Executor)
                  (implicit threadNamer: ThreadNamer = IdentityThreadNamer): Executor = new ContinuityExecutor(executor)(threadNamer)

  def wrapExecutorService(executor: ExecutorService)
                         (implicit threadNamer: ThreadNamer = IdentityThreadNamer): ExecutorService = new ContinuityExecutorService(executor)(threadNamer)

  def wrapCompletableFutureExecutorService(executor: CompletableFutureExecutorService)
                                          (implicit threadNamer: ThreadNamer = IdentityThreadNamer): CompletableFutureExecutorService = {
    new ContinuityCompletableFutureExecutorService(executor)(threadNamer)
  }

}
