package com.avast.continuity

import java.util.concurrent.{Callable, ScheduledExecutorService, ScheduledFuture, TimeUnit}

class ContinuityScheduledExecutorService(executor: ScheduledExecutorService)(implicit threadNamer: ThreadNamer)
    extends ContinuityExecutorService(executor)
    with ScheduledExecutorService {
  override def scheduleWithFixedDelay(runnable: Runnable, l: Long, l1: Long, timeUnit: TimeUnit): ScheduledFuture[_] =
    executor.scheduleWithFixedDelay(new MdcRunnable(runnable), l, l1, timeUnit)

  override def scheduleAtFixedRate(runnable: Runnable, l: Long, l1: Long, timeUnit: TimeUnit): ScheduledFuture[_] =
    executor.scheduleAtFixedRate(new MdcRunnable(runnable), l, l1, timeUnit)

  override def schedule(runnable: Runnable, l: Long, timeUnit: TimeUnit): ScheduledFuture[_] =
    executor.schedule(new MdcRunnable(runnable), l, timeUnit)

  override def schedule[V](callable: Callable[V], l: Long, timeUnit: TimeUnit): ScheduledFuture[V] =
    executor.schedule(new MdcCallable[V](callable), l, timeUnit)
}
