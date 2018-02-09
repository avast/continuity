package com.avast.continuity.javaapi;

import com.avast.continuity.Continuity$;
import com.avast.continuity.IdentityThreadNamer$;
import com.avast.continuity.ThreadNamer;
import scala.Option;
import scala.runtime.AbstractFunction0;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/* KEEP THIS IMPLEMENTATION IN SYNC WITH THE SCALA API VERSION */

/**
 * Provides methods to work with the Continuity context and factory methods to wrap thread pools.
 */
public final class Continuity {

    private static final Continuity$ CONTINUITY = Continuity$.MODULE$;

    /**
     * Marks that the current thread was already processed.
     */
    private static final String MARKER = "__MARKER__";

    private Continuity() {
    }

    /**
     * <p>Puts the given values into the context, names a thread and runs the given block of code.
     * It correctly cleans up everything after the block finishes.</p>
     * <p>This method is to be used at the leaves of Continuity context usage meaning that you have to fill in the context
     * somewhere and this method should be used for that. From there the context is propagated automatically.</p>
     */
    public static <T> T withContext(Map<String, String> ctxValues, Callable<T> block) throws RuntimeException {
        return withContext(ctxValues, IdentityThreadNamer$.MODULE$, block);
    }

    /**
     * <p>Puts the given values into the context, names a thread and runs the given block of code.
     * It correctly cleans up everything after the block finishes.</p>
     * <p>This method is to be used at the leaves of Continuity context usage meaning that you have to fill in the context
     * somewhere and this method should be used for that. From there the context is propagated automatically.</p>
     */
    public static <T> T withContext(Map<String, String> ctxValues, ThreadNamer threadNamer, Callable<T> block) throws RuntimeException {
        if (getFromContext(MARKER).isPresent()) {
            try {
                return block.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                putToContext(MARKER, "");
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
                removeFromContext(MARKER);
            }
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

    public static ScheduledExecutorService wrapScheduledExecutorService(ScheduledExecutorService executor) {
        return CONTINUITY.wrapScheduledExecutorService(executor, IdentityThreadNamer$.MODULE$);
    }

    public static ScheduledExecutorService wrapScheduledExecutorService(ScheduledExecutorService executor, ThreadNamer threadNamer) {
        return CONTINUITY.wrapScheduledExecutorService(executor, threadNamer);
    }

    private static <T> Optional<T> optionToOptional(Option<T> option) {
        if (option.isDefined()) {
            return Optional.of(option.get());
        } else {
            return Optional.empty();
        }
    }

}
