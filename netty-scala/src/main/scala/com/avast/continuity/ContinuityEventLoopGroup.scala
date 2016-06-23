package com.avast.continuity

import java.util
import java.util.concurrent.{Callable, TimeUnit}

import io.netty.channel.{Channel, ChannelFuture, ChannelPromise, EventLoop, EventLoopGroup}
import io.netty.util.concurrent.{EventExecutor, Future, ScheduledFuture}

import scala.collection.JavaConverters._

class ContinuityEventLoopGroup(executor: EventLoopGroup)(implicit threadNamer: ThreadNamer) extends EventLoopGroup {

  override def execute(runnable: Runnable): Unit = executor.execute(new MdcRunnable(runnable))

  override def submit(task: Runnable): Future[_] = executor.submit(new MdcRunnable(task))

  override def submit[T](task: Runnable, result: T): Future[T] = executor.submit(new MdcRunnable(task), result)

  override def submit[T](task: Callable[T]): Future[T] = executor.submit(new MdcCallable(task))

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]]): T = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAny(mappedTasks)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]]): util.List[util.concurrent.Future[T]] = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAll(mappedTasks)
  }

  override def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAny(mappedTasks, timeout, unit)
  }

  override def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[util.concurrent.Future[T]] = {
    val mappedTasks = tasks.asScala.map(new MdcCallable(_)).toList.asJava
    executor.invokeAll(mappedTasks, timeout, unit)
  }

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] = executor.schedule(new MdcRunnable(command), delay, unit)

  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V] = executor.schedule(new MdcCallable(callable), delay, unit)

  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_] = {
    executor.scheduleAtFixedRate(new MdcRunnable(command), initialDelay, period, unit)
  }

  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_] = {
    executor.scheduleWithFixedDelay(new MdcRunnable(command), initialDelay, delay, unit)
  }

  override def shutdown(): Unit = executor.shutdown()

  override def shutdownNow(): util.List[Runnable] = executor.shutdownNow()

  override def isShutdown: Boolean = executor.isShutdown

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = executor.awaitTermination(timeout, unit)

  override def isTerminated: Boolean = executor.isTerminated

  override def next(): EventLoop = executor.next()

  override def register(channel: Channel): ChannelFuture = executor.register(channel)

  override def register(channel: Channel, promise: ChannelPromise): ChannelFuture = executor.register(channel, promise)

  override def isShuttingDown: Boolean = executor.isShuttingDown

  override def terminationFuture(): Future[_] = executor.terminationFuture()

  override def iterator(): util.Iterator[EventExecutor] = executor.iterator()

  override def shutdownGracefully(): Future[_] = executor.shutdownGracefully()

  override def shutdownGracefully(quietPeriod: Long, timeout: Long, unit: TimeUnit): Future[_] = {
    executor.shutdownGracefully(quietPeriod, timeout, unit)
  }

  override def register(promise: ChannelPromise): ChannelFuture = executor.register(promise)

}
