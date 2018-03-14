package com.avast.continuity.monix

import com.avast.continuity.{IdentityThreadNamer, ThreadNamer}
import monix.execution.Scheduler

object Monix {
  def wrapScheduler(s: Scheduler)(implicit threadNamer: ThreadNamer = IdentityThreadNamer): Scheduler = new ContinuityScheduler(s)
}
