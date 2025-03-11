package com.reactivespring.config;

import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.function.Function;

public class RetryUtil {

    public static <T extends Throwable> Retry retrySpec(Class<T> exceptionClass, Function<Retry.RetrySignal, ? extends T> exceptionSupplier) {
        return Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> exceptionClass.isInstance(throwable))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        exceptionSupplier.apply(retrySignal));
    }
}