package com.avast.continuity

import java.util.concurrent.{Executor, Executors}

import org.scalatest.FunSuite
import org.scalatest.concurrent.AsyncAssertions
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

class ContinuityTest extends FunSuite with AsyncAssertions {

  /** See the log output to check that traceId is present. */
  private val logger = LoggerFactory.getLogger(this.getClass)

  test("traceId") {
    testTraceId(Executors.newSingleThreadExecutor)
    testTraceId(ExecutionContext.global)
  }

  private def testTraceId(pool: Executor) {
    val waiter = new Waiter

    val wrappedPool = Continuity.wrapExecutor(pool, ContinuityContextThreadNamer.prefix("traceId"))

    val traceId1 = "id1"
    Continuity.putToContext("traceId", traceId1)
    wrappedPool.execute(new Runnable {
      override def run(): Unit = {
        assert(Continuity.getFromContext("traceId") === Some(traceId1))
        logger.info("first")
        waiter.dismiss()
      }
    })

    val traceId2 = "id2"
    Continuity.putToContext("traceId", traceId2)
    pool.execute(new Runnable {
      override def run(): Unit = {
        assert(Continuity.getFromContext("traceId") === None)
        logger.info("second")
        waiter.dismiss()
      }
    })

    val traceId3 = "id3"
    Continuity.putToContext("traceId", traceId3)
    wrappedPool.execute(new Runnable {
      override def run(): Unit = {
        assert(Continuity.getFromContext("traceId") === Some(traceId3))
        logger.info("third")
        waiter.dismiss()
      }
    })

    waiter.await(dismissals(3))
  }
}
