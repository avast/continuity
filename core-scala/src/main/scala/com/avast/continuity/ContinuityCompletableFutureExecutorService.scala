package com.avast.continuity

import java.util
import java.util.concurrent.{Callable, Future, TimeUnit}

import com.avast.utils2.concurrent.CompletableFutureExecutorService

import scala.collection.JavaConverters._

class ContinuityCompletableFutureExecutorService(executor: CompletableFutureExecutorService)
                                                (implicit threadNamer: ThreadNamer)
  extends CompletableFutureExecutorService
    with ContinuityExecutorMarker {

  override def execute(runnable: Runnable): Unit = executor.execute(new MdcRunnable(runnable))

  override def submit(task: Runnable): Future[_] = executor.submit(new MdcRunnable(task))

  override def submit[T](task: Runnable, result: T): Future[T] = executor.submit(new MdcRunnable(task), result)

  override def submit[T](task: Callable[T]): Future[T] = executor.submit(new MdcCallable(task))

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]]): T = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAny(mappedTasks)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]]): java.util.List[Future[T]] = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAll(mappedTasks)
  }

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAny(mappedTasks, timeout, unit)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): java.util.List[Future[T]] = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAll(mappedTasks, timeout, unit)
  }

  override def shutdown(): Unit = executor.shutdown()

  override def shutdownNow(): util.List[Runnable] = executor.shutdownNow()

  override def isShutdown: Boolean = executor.isShutdown

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = executor.awaitTermination(timeout, unit)

  override def isTerminated: Boolean = executor.isTerminated

}
