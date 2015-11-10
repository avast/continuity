package com.avast.continuity

import scala.concurrent.ExecutionContext

class ContinuityExecutionContext(ec: ExecutionContext) extends ExecutionContext {

  override def execute(runnable: Runnable) = ec.execute(new MdcRunnable(runnable))

  override def reportFailure(cause: Throwable) = ec.reportFailure(cause)

}
