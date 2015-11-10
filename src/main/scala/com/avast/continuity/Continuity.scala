package com.avast.continuity

import java.util.concurrent.{Executor, ExecutorService}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}

trait Continuity {

  protected[this] def getFromContext(key: String): Option[String] = Continuity.getFromContext(key)

  protected[this] def putToContext(key: String, value: String): Unit = Continuity.putToContext(key, value)

  protected[this] def removeFromContext(key: String): Unit = Continuity.removeFromContext(key)

}

object Continuity {

  def getFromContext(key: String): Option[String] = context.get.get(key)

  def putToContext(key: String, value: String): Unit = {
    val ctx = context.get
    context.set(ctx + (key -> value))
  }

  def removeFromContext(key: String): Unit = {
    val ctx = context.get
    context.set(ctx - key)
  }

  def wrapExecutionContext(executor: ExecutionContext,
                           threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContext = new ContinuityExecutionContext(executor)(threadNamer)

  def wrapExecutionContextExecutor(executor: ExecutionContextExecutor,
                                   threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContextExecutor = {
    new ContinuityExecutionContextExecutor(executor)(threadNamer)
  }

  def wrapExecutionContextExecutorService(executor: ExecutionContextExecutorService,
                                          threadNamer: ThreadNamer = IdentityThreadNamer): ExecutionContextExecutorService = {
    new ContinuityExecutionContextExecutorService(executor)(threadNamer)
  }

  def wrapExecutor(executor: Executor,
                   threadNamer: ThreadNamer = IdentityThreadNamer): Executor = new ContinuityExecutor(executor)(threadNamer)

  def wrapExecutorService(executor: ExecutorService,
                          threadNamer: ThreadNamer = IdentityThreadNamer): Executor = new ContinuityExecutorService(executor)(threadNamer)

}
