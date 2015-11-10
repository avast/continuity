package com.avast.continuity

import scala.concurrent.ExecutionContextExecutor

class ContinuityExecutionContextExecutor(ec: ExecutionContextExecutor) extends ExecutionContextExecutor {

  override def execute(runnable: Runnable) = ec.execute(new MdcRunnable(runnable))

  override def reportFailure(cause: Throwable) = ec.reportFailure(cause)

}
