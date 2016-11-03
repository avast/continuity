package com.avast.continuity

import java.util.concurrent.{Executor, Executors}

import com.avast.utils2.concurrent.ConfigurableThreadFactory
import com.avast.utils2.concurrent.ConfigurableThreadFactory.IndexingNamingStrategy
import org.scalatest.FunSuite
import org.scalatest.concurrent.Waiters
import org.scalatest.time.{Seconds, Span}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ContinuityTest extends FunSuite with Waiters {

  /** See the log output to check that traceId is present. */
  private val logger = LoggerFactory.getLogger(this.getClass)

  test("traceId") {
    testTraceId(Executors.newSingleThreadExecutor)
    testTraceId(ExecutionContext.global)
  }

  private def testTraceId(pool: Executor): Unit = {
    val waiter = new Waiter

    implicit val threadNamer = ContinuityContextThreadNamer.prefix("traceId")
    val wrappedPool = Continuity.wrapExecutor(pool)

    val traceId1 = "id1"
    Continuity.withContext("traceId" -> traceId1) {
      wrappedPool.execute(new Runnable {
        override def run(): Unit = {
          assert(Continuity.getFromContext("traceId") === Some(traceId1))
          logger.info("first")
          waiter.dismiss()
        }
      })
    }

    val traceId2 = "id2"
    Continuity.withContext("traceId" -> traceId2) {
      pool.execute(new Runnable {
        override def run(): Unit = {
          assert(Continuity.getFromContext("traceId") === None)
          logger.info("second")
          waiter.dismiss()
        }
      })
    }

    val traceId3 = "id3"
    Continuity.withContext("traceId" -> traceId3) {
      wrappedPool.execute(new Runnable {
        override def run(): Unit = {
          assert(Continuity.getFromContext("traceId") === Some(traceId3))
          logger.info("third")
          waiter.dismiss()
        }
      })
    }

    waiter.await(dismissals(3))
  }

  test("futures") {
    implicit val ec = Continuity.wrapExecutionContext(ExecutionContext.global)
    val f = Continuity.withContext("traceId" -> "value") {
      Future {
        assert(Continuity.getFromContext("traceId") === Some("value"))
        logger.info(s"creating future ${ Continuity.getFromContext("traceId") }")
        1
      }.map { i =>
        assert(Continuity.getFromContext("traceId") === Some("value"))
        logger.info(s"mapping future ${ Continuity.getFromContext("traceId") }")
        i + 1
      }
    }

    assert(Await.result(f, Duration("1s")) === 2)
  }

  test("double wrapping") {
    val waiter = new Waiter

    implicit val namer = ContinuityContextThreadNamer.prefix("traceId")
    val pool = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(1, ConfigurableThreadFactory.builder.withNamingStrategy(new IndexingNamingStrategy("thread-%d")).build))
    val target = Continuity.wrapExecutionContext(Continuity.wrapExecutionContext(pool))

    val traceId1 = "id1"
    Continuity.withContext("traceId" -> traceId1) {
      target.execute(new Runnable {
        override def run(): Unit = {
          println("running")
          assert(Continuity.getFromContext("traceId") === Some(traceId1))
          assert(Thread.currentThread.getName.startsWith("id1-thread-0"))
          waiter.dismiss()
        }
      })
    }

    waiter.await(timeout(Span(5, Seconds)), dismissals(1))
  }

}
