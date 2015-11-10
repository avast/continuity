package com.avast.continuity

import java.util.concurrent.{Callable, Future, TimeUnit}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutorService

class ContinuityExecutionContextExecutorService(ec: ExecutionContextExecutorService) extends ExecutionContextExecutorService {

  override def execute(runnable: Runnable) = ec.execute(new MdcRunnable(runnable))

  override def reportFailure(cause: Throwable) = ec.reportFailure(cause)

  override def submit(task: Runnable): Future[_] = ec.submit(new MdcRunnable(task))

  override def submit[T](task: Runnable, result: T): Future[T] = ec.submit(new MdcRunnable(task), result)

  override def submit[T](task: Callable[T]): Future[T] = ec.submit(new MdcCallable(task))

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]]): T = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    ec.invokeAny(mappedTasks)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]]): java.util.List[Future[T]] = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    ec.invokeAll(mappedTasks)
  }

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    ec.invokeAny(mappedTasks, timeout, unit)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): java.util.List[Future[T]] = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    ec.invokeAll(mappedTasks, timeout, unit)
  }

  override def shutdown() = ec.shutdown()

  override def shutdownNow() = ec.shutdownNow()

  override def isShutdown = ec.isShutdown

  override def awaitTermination(timeout: Long, unit: TimeUnit) = ec.awaitTermination(timeout, unit)

  override def isTerminated = ec.isTerminated

}
