package com.avast.continuity.monix

import com.avast.continuity.Continuity
import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class ContinuitySchedulerTest extends FunSuite with ScalaFutures {

  /** See the log output to check that traceId is present. */
  private val logger = LoggerFactory.getLogger(this.getClass)

  test("basic") {
    implicit val sch = Monix.wrapScheduler(Scheduler.global)

    val res = Continuity
      .withContext("traceId" -> "value") {
        Task {
          assert(Continuity.getFromContext("traceId") === Some("value"))
          logger.info(s"creating future ${Continuity.getFromContext("traceId")}")
          1
        }.map { i =>
          assert(Continuity.getFromContext("traceId") === Some("value"))
          logger.info(s"mapping future ${Continuity.getFromContext("traceId")}")
          i + 1
        }.runAsync
      }
      .futureValue

    assertResult(2)(res)
  }

  test("futures") {
    implicit val sch = Monix.wrapScheduler(Scheduler.global)

    val res = Continuity
      .withContext("traceId" -> "value") {
        Future {
          assert(Continuity.getFromContext("traceId") === Some("value"))
          logger.info(s"creating future ${Continuity.getFromContext("traceId")}")
          1
        }.map { i =>
          assert(Continuity.getFromContext("traceId") === Some("value"))
          logger.info(s"mapping future ${Continuity.getFromContext("traceId")}")
          i + 1
        }
      }
      .futureValue

    assertResult(2)(res)
  }
}
