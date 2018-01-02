package com.avast.continuity

import scala.concurrent.ExecutionContextExecutor

class ContinuityExecutionContextExecutor(ec: ExecutionContextExecutor)(implicit threadNamer: ThreadNamer)
    extends ExecutionContextExecutor
    with ContinuityExecutorMarker {

  override def execute(runnable: Runnable): Unit = ec.execute(new MdcRunnable(runnable))

  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)

}
