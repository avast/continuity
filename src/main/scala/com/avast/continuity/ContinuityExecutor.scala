package com.avast.continuity

import java.util.concurrent.Executor

class ContinuityExecutor(executor: Executor) extends Executor {

  override def execute(runnable: Runnable) = executor.execute(new MdcRunnable(runnable))

}
