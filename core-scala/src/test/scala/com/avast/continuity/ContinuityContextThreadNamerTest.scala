package com.avast.continuity

import org.scalatest.FunSuite

class ContinuityContextThreadNamerTest extends FunSuite {

  test("continuity thread naming") {
    Thread.currentThread.setName("TestThread")

    assert(Thread.currentThread.getName === "TestThread")

    val namer1 = new ContinuityContextThreadNamer("")
    namer1.nameThread {
      assert(Thread.currentThread.getName === "TestThread")
    }

    assert(Thread.currentThread.getName === "TestThread")

    val namer2 = new ContinuityContextThreadNamer("%thread%")
    namer2.nameThread {
      assert(Thread.currentThread.getName === "TestThread")
    }

    val namer3 = new ContinuityContextThreadNamer("[%id%]-%thread%")
    namer3.nameThread {
      assert(Thread.currentThread.getName === "[unknown]-TestThread")
    }

    Continuity.putToContext("id", "TestId")
    val namer4 = new ContinuityContextThreadNamer("[%id%]-%thread%")
    namer4.nameThread {
      assert(Thread.currentThread.getName === "[TestId]-TestThread")
    }
  }

}
