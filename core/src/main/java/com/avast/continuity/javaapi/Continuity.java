package com.avast.continuity.javaapi;

import com.avast.continuity.Continuity$;
import com.avast.continuity.IdentityThreadNamer$;
import com.avast.continuity.ThreadNamer;
import scala.Option;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public final class Continuity {

    private static final Continuity$ CONTINUITY = Continuity$.MODULE$;

    private Continuity() {
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

    public static Executor wrapExecutorService(ExecutorService executor) {
        return CONTINUITY.wrapExecutorService(executor, IdentityThreadNamer$.MODULE$);
    }

    public static Executor wrapExecutorService(ExecutorService executor, ThreadNamer threadNamer) {
        return CONTINUITY.wrapExecutorService(executor, threadNamer);
    }

    private static <T> Optional<T> optionToOptional(Option<T> option) {
        if (option.isDefined()) {
            return Optional.of(option.get());
        } else {
            return Optional.empty();
        }
    }

}
