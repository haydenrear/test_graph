package com.hayden.test_graph.action;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Aspect
@Component
public class IdempotentAspect {

    private final ConcurrentHashMap<CacheValue, DelayedAction> delays = new ConcurrentHashMap<>();

    record CacheValue(Object method, Object target) { }

    record DelayedAction(LocalDateTime expireFutureTime, CacheValue cacheValue, Object ret) implements Delayed {

        DelayedAction(CacheValue cacheValue, Object ret) {
            this(LocalDateTime.now(), cacheValue, ret);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            if (o instanceof DelayedAction d) {
                return this.expireFutureTime.compareTo(d.expireFutureTime);
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(Duration.between(LocalDateTime.now(), expireFutureTime));
        }
    }

    private static final Object voidObj = new Object();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) {

        var method = joinPoint.getSignature().getName();
        var targetName = joinPoint.getTarget().getClass().getName();

        CacheValue cv = new CacheValue("%s.%s".formatted(targetName, method), joinPoint.getTarget());

        var r = delays.compute(
                cv,
                (key, prev) -> Optional.ofNullable(prev)
                        .map(p -> callIfNotExpiredOrNotCached(joinPoint, cv, idempotent, prev))
                        .orElseGet(() -> getProceedToAdd(joinPoint, cv))
        );

        return r.ret == voidObj ? null : r.ret;
    }

    private DelayedAction callIfNotExpiredOrNotCached(ProceedingJoinPoint joinPoint, CacheValue cv, Idempotent idempotent, DelayedAction prev) {
        if (prev == voidObj && isNotTimed(idempotent))
            return prev;
        else if (prev == null) {
            return getProceedToAdd(joinPoint, cv);
        } else if (isNotTimed(idempotent)) {
            Assert.notNull(prev, "Assumed not to be null.");
            return prev;
        }
        long delay = prev.getDelay(TimeUnit.MILLISECONDS);
        if (delay <= 0) {
            return getProceedToAdd(joinPoint, cv);
        } else {
            return prev;
        }
    }

    private static boolean isNotTimed(Idempotent idempotent) {
        return idempotent.timeoutMillis() <= 0L;
    }


    @SneakyThrows
    private DelayedAction getProceedToAdd(ProceedingJoinPoint joinPoint, CacheValue cv) {
        return Optional.ofNullable(joinPoint.proceed())
                .or(() -> Optional.of(voidObj))
                .map(o -> new DelayedAction(cv, o))
                .get();
    }
}
