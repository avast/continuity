package com.avast.continuity

import java.util.concurrent.{Callable, TimeUnit}

import io.netty.channel.{Channel, ChannelPromise, EventLoopGroup}

import scala.collection.JavaConverters._

class ContinuityEventLoopGroup(executor: EventLoopGroup)(implicit threadNamer: ThreadNamer) extends EventLoopGroup {

  override def execute(runnable: Runnable) = executor.execute(new MdcRunnable(runnable))

  override def submit(task: Runnable) = executor.submit(new MdcRunnable(task))

  override def submit[T](task: Runnable, result: T) = executor.submit(new MdcRunnable(task), result)

  override def submit[T](task: Callable[T]) = executor.submit(new MdcCallable(task))

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]]) = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAny(mappedTasks)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]]) = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAll(mappedTasks)
  }

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit) = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAny(mappedTasks, timeout, unit)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit) = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAll(mappedTasks, timeout, unit)
  }

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit) = executor.schedule(new MdcRunnable(command), delay, unit)

  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit) = executor.schedule(new MdcCallable(callable), delay, unit)

  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit) = {
    executor.scheduleAtFixedRate(new MdcRunnable(command), initialDelay, period, unit)
  }

  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) = {
    executor.scheduleWithFixedDelay(new MdcRunnable(command), initialDelay, delay, unit)
  }

  override def shutdown() = executor.shutdown()

  override def shutdownNow() = executor.shutdownNow()

  override def isShutdown = executor.isShutdown

  override def awaitTermination(timeout: Long, unit: TimeUnit) = executor.awaitTermination(timeout, unit)

  override def isTerminated = executor.isTerminated

  override def next() = executor.next()

  override def register(channel: Channel) = executor.register(channel)

  override def register(channel: Channel, promise: ChannelPromise) = executor.register(channel, promise)

  override def isShuttingDown = executor.isShuttingDown

  override def terminationFuture() = executor.terminationFuture()

  override def iterator() = executor.iterator()

  override def shutdownGracefully() = executor.shutdownGracefully()

  override def shutdownGracefully(quietPeriod: Long, timeout: Long, unit: TimeUnit) = executor.shutdownGracefully(quietPeriod, timeout, unit)

}
