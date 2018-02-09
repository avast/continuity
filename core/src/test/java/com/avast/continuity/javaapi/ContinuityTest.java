package com.avast.continuity.javaapi;

import com.avast.continuity.ContinuityContextThreadNamer;
import com.avast.continuity.ThreadNamer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class ContinuityTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testExecutorTraceId() throws InterruptedException {
        ThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        Executor original = Executors.newCachedThreadPool();
        Executor wrapped = Continuity.wrapExecutor(original, threadNamer);

        testExecuteTraceId(threadNamer, original, wrapped);
    }

    @Test
    public void testExecutorServiceTraceId() throws InterruptedException {
        ThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        ExecutorService original = Executors.newCachedThreadPool();
        ExecutorService wrapped = Continuity.wrapExecutorService(original, threadNamer);

        testExecuteTraceId(threadNamer, original, wrapped);
    }

    @Test
    public void testScheduledExecutorServiceTraceId() throws InterruptedException {
        ThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        ScheduledExecutorService original = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService wrapped = Continuity.wrapScheduledExecutorService(original, threadNamer);

        testExecuteTraceId(threadNamer, original, wrapped);
        testScheduleTraceId(threadNamer, original, wrapped);
    }

    private void testExecuteTraceId(ThreadNamer threadNamer, Executor original, Executor wrapped) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        String traceId1 = "id1";
        Continuity.withContext(map("traceId", traceId1), threadNamer, () -> {
            wrapped.execute(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.of(traceId1));
                logger.info("first");
            });
            latch.countDown();
            return null;
        });

        String traceId2 = "id2";
        Continuity.withContext(map("traceId", traceId2), threadNamer, () -> {
            original.execute(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.empty());
                logger.info("second");
            });
            latch.countDown();
            return null;
        });

        String traceId3 = "id3";
        Continuity.withContext(map("traceId", traceId3), threadNamer, () -> {
            wrapped.execute(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.of(traceId3));
                logger.info("third");
            });
            latch.countDown();
            return null;
        });

        latch.await();
        Thread.sleep(500); // wait for the logger to write out its buffer
    }

    private void testScheduleTraceId(ThreadNamer threadNamer, ScheduledExecutorService original, ScheduledExecutorService wrapped) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        String traceId1 = "id1";
        Continuity.withContext(map("traceId", traceId1), threadNamer, () -> {
            wrapped.schedule(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.of(traceId1));
                logger.info("first");
            }, 10, TimeUnit.MILLISECONDS);
            latch.countDown();
            return null;
        });

        String traceId2 = "id2";
        Continuity.withContext(map("traceId", traceId2), threadNamer, () -> {
            original.schedule(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.empty());
                logger.info("second");
            }, 10, TimeUnit.MILLISECONDS);
            latch.countDown();
            return null;
        });

        String traceId3 = "id3";
        Continuity.withContext(map("traceId", traceId3), threadNamer, () -> {
            wrapped.schedule(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.of(traceId3));
                logger.info("third");
            }, 10, TimeUnit.MILLISECONDS);
            latch.countDown();
            return null;
        });

        latch.await();
        Thread.sleep(500); // wait for the logger to write out its buffer
    }

    @Test
    public void testExecutorFutures() throws Exception {
        ContinuityContextThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        Executor wrapped = Continuity.wrapExecutor(Executors.newCachedThreadPool(), threadNamer);

        testFutures(threadNamer, wrapped);
    }

    @Test
    public void testExecutorServiceFutures() throws Exception {
        ContinuityContextThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        ExecutorService wrapped = Continuity.wrapExecutorService(Executors.newCachedThreadPool(), threadNamer);

        testFutures(threadNamer, wrapped);
    }

    @Test
    public void testScheduledExecutorServiceFutures() throws Exception {
        ContinuityContextThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        ScheduledExecutorService wrapped = Continuity.wrapScheduledExecutorService(Executors.newSingleThreadScheduledExecutor(), threadNamer);

        testFutures(threadNamer, wrapped);
    }

    private void testFutures(ThreadNamer threadNamer, Executor wrapped) throws Exception {
        CompletableFuture<Integer> f = Continuity.withContext(map("traceId", "value"), threadNamer, () -> {
            CompletableFuture<Integer> x = new CompletableFuture<>();
            x.whenCompleteAsync((a, b) -> {
                assertEquals("value", Continuity.getFromContext("traceId"));
                logger.info("inside completablefuture " + Continuity.getFromContext("traceId"));
            }, wrapped);
            x.complete(1);
            return x;
        });

        f.get();
        Thread.sleep(500); // wait for the logger to write out its buffer
    }

    private Map<String, String> map(String key, String value) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

}
