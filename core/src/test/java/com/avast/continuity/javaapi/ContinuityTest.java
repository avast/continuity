package com.avast.continuity.javaapi;

import com.avast.continuity.ContinuityContextThreadNamer;
import com.avast.continuity.ThreadNamer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class ContinuityTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testTraceId() throws InterruptedException {
        ThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");

        ExecutorService executor = Executors.newCachedThreadPool();
        ExecutorService wrappedExecutor = Continuity.wrapExecutorService(executor, threadNamer);

        CountDownLatch latch = new CountDownLatch(3);

        String traceId1 = "id1";
        Continuity continuity1 = new Continuity(map("traceId", traceId1), threadNamer);
        continuity1.withContext(() -> {
            wrappedExecutor.execute(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.of(traceId1));
                logger.info("first");
            });
            latch.countDown();
            return null;
        });

        String traceId2 = "id2";
        Continuity continuity2 = new Continuity(map("traceId", traceId2), threadNamer);
        continuity2.withContext(() -> {
            executor.execute(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.empty());
                logger.info("second");
            });
            latch.countDown();
            return null;
        });

        String traceId3 = "id3";
        Continuity continuity3 = new Continuity(map("traceId", traceId3), threadNamer);
        continuity3.withContext(() -> {
            wrappedExecutor.execute(() -> {
                assertEquals(Continuity.getFromContext("traceId"), Optional.of(traceId3));
                logger.info("third");
            });
            latch.countDown();
            return null;
        });

        latch.await();
        Thread.sleep(500); // wait for the logger to write out its buffer
    }

    @Test
    public void testFutures() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        ContinuityContextThreadNamer threadNamer = ContinuityContextThreadNamer.prefix("traceId");
        ExecutorService wrapExecutorService = Continuity.wrapExecutorService(executor, threadNamer);

        Continuity c = new Continuity(map("traceId", "value"), threadNamer);

        CompletableFuture<Integer> f = c.withContext(() -> {
            CompletableFuture<Integer> x = new CompletableFuture<Integer>();
            x.whenCompleteAsync((a, b) -> {
                assertEquals("value", Continuity.getFromContext("traceId"));
                logger.info("inside completablefuture " + Continuity.getFromContext("traceId"));
            }, wrapExecutorService);
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
