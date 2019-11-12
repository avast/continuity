package com.avast.continuity.monix

import java.util.concurrent.TimeUnit

import com.avast.continuity.{MdcRunnable, ThreadNamer}
import monix.execution.{Cancelable, ExecutionModel, Scheduler, UncaughtExceptionReporter}

class ContinuityScheduler(wrapped: Scheduler)(implicit threadNamer: ThreadNamer) extends Scheduler {
  override def execute(command: Runnable): Unit = {
    wrapped.execute(new MdcRunnable(command))
  }

  override def reportFailure(t: Throwable): Unit = {
    wrapped.reportFailure(t)
  }

  override def scheduleOnce(initialDelay: Long, unit: TimeUnit, r: Runnable): Cancelable = {
    wrapped.scheduleOnce(initialDelay, unit, new MdcRunnable(r))
  }

  override def scheduleWithFixedDelay(initialDelay: Long, delay: Long, unit: TimeUnit, r: Runnable): Cancelable = {
    wrapped.scheduleWithFixedDelay(initialDelay, delay, unit, new MdcRunnable(r))
  }

  override def scheduleAtFixedRate(initialDelay: Long, period: Long, unit: TimeUnit, r: Runnable): Cancelable = {
    wrapped.scheduleAtFixedRate(initialDelay, period, unit, new MdcRunnable(r))
  }

  override def clockRealTime(unit: TimeUnit): Long = wrapped.clockRealTime(unit)

  override def clockMonotonic(unit: TimeUnit): Long = wrapped.clockMonotonic(unit)

  override def withUncaughtExceptionReporter(r: UncaughtExceptionReporter): Scheduler =
    new ContinuityScheduler(wrapped.withUncaughtExceptionReporter(r))

  override def executionModel: ExecutionModel = wrapped.executionModel

  override def withExecutionModel(em: ExecutionModel): Scheduler = new ContinuityScheduler(wrapped.withExecutionModel(em))
}
