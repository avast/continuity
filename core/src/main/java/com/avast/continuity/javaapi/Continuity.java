package com.avast.continuity.javaapi;

import com.avast.continuity.Continuity$;
import com.avast.continuity.IdentityThreadNamer$;
import com.avast.continuity.ThreadNamer;
import com.avast.utils2.concurrent.CompletableFutureExecutorService;
import scala.Option;
import scala.runtime.AbstractFunction0;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public final class Continuity {

    private static final Continuity$ CONTINUITY = Continuity$.MODULE$;

    private final Map<String, String> ctxValues;
    private final ThreadNamer threadNamer;

    public Continuity(Map<String, String> ctxValues, ThreadNamer threadNamer) {
        this.ctxValues = ctxValues;
        this.threadNamer = threadNamer;
    }

    public Continuity(Map<String, String> ctxValues) {
        this(ctxValues, IdentityThreadNamer$.MODULE$);
    }

    public Continuity() {
        this(Collections.<String, String>emptyMap());
    }

    public <T> T withContext(final Callable<T> block) throws RuntimeException {
        try {
            ctxValues.forEach(Continuity::putToContext);
            return threadNamer.nameThread(new AbstractFunction0<T>() {
                @Override
                public T apply() {
                    try {
                        return block.call();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } finally {
            ctxValues.forEach((k, v) -> removeFromContext(k));
        }
    }

    public static Optional<String> getFromContext(String key) {
        return optionToOptional(CONTINUITY.getFromContext(key));
    }

    public static void putToContext(String key, String value) {
        CONTINUITY.putToContext(key, value);
    }

    public static void removeFromContext(String key) {
        CONTINUITY.removeFromContext(key);
    }

    public static Executor wrapExecutor(Executor executor) {
        return CONTINUITY.wrapExecutor(executor, IdentityThreadNamer$.MODULE$);
    }

    public static Executor wrapExecutor(Executor executor, ThreadNamer threadNamer) {
        return CONTINUITY.wrapExecutor(executor, threadNamer);
    }

    public static ExecutorService wrapExecutorService(ExecutorService executor) {
        return CONTINUITY.wrapExecutorService(executor, IdentityThreadNamer$.MODULE$);
    }

    public static ExecutorService wrapExecutorService(ExecutorService executor, ThreadNamer threadNamer) {
        return CONTINUITY.wrapExecutorService(executor, threadNamer);
    }

    public static CompletableFutureExecutorService wrapCompletableFutureExecutorService(CompletableFutureExecutorService executor) {
        return CONTINUITY.wrapCompletableFutureExecutorService(executor, IdentityThreadNamer$.MODULE$);
    }

    public static CompletableFutureExecutorService wrapCompletableFutureExecutorService(CompletableFutureExecutorService executor, ThreadNamer threadNamer) {
        return CONTINUITY.wrapCompletableFutureExecutorService(executor, threadNamer);
    }

    private static <T> Optional<T> optionToOptional(Option<T> option) {
        if (option.isDefined()) {
            return Optional.of(option.get());
        } else {
            return Optional.empty();
        }
    }

}
