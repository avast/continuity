package com.avast.continuity

import scala.concurrent.ExecutionContext

class ContinuityExecutionContext(ec: ExecutionContext)(implicit threadNamer: ThreadNamer) extends ExecutionContext
                                                                                                  with ContinuityExecutorMarker {

  override def execute(runnable: Runnable) = ec.execute(new MdcRunnable(runnable))

  override def reportFailure(cause: Throwable) = ec.reportFailure(cause)

}
