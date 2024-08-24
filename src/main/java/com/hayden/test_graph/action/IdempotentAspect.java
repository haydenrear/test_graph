package com.hayden.test_graph.action;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class IdempotentAspect {

    private final ConcurrentHashMap<CacheValue, Object> did = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<CacheValue, DelayedAction> delays = new ConcurrentHashMap<>();

    record CacheValue(Object method, Object target) { }

    record DelayedAction(LocalDateTime expireFutureTime, CacheValue cacheValue) implements Delayed {

        DelayedAction(CacheValue cacheValue) {
            this(LocalDateTime.now(), cacheValue);
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

    private static final Object nullObj = new Object();

    private static final Object voidObj = new Object();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {

        var t = joinPoint.getTarget();
        var method = t.getClass().getMethod(joinPoint.getSignature().getName(), Arrays.stream(joinPoint.getArgs()).map(Object::getClass).toArray(Class[]::new));

        CacheValue cv = new CacheValue(method, joinPoint.getTarget());


        if (idempotent.timeoutMillis() > 0L && !delays.containsKey(cv)) {
            delays.put(cv, new DelayedAction(cv));
        }

        if (did.compute(cv, (key, prev) -> {
            if (prev == null) {
                return nullObj;
            }

            return prev;
        }) == nullObj) {
            synchronized (joinPoint.getTarget()) {
                if (did.get(cv) == nullObj) {
                    return Optional.ofNullable(joinPoint.proceed())
                            .map(o -> {
                                did.put(cv, o);
                                return o;
                            })
                            .orElseGet(() -> {
                                did.put(cv, voidObj);
                                return null;
                            });
                } else {
                    return did.get(cv);
                }
            }
        }

        if (did.get(cv) == voidObj && !delays.containsKey(cv)) {
            return null;
        } else if (!delays.containsKey(cv)) {
            return did.get(cv);
        } else {
            var delayed = delays.get(cv);
            if (delayed.getDelay(TimeUnit.MILLISECONDS) <= 0) {
                var nextValue = joinPoint.proceed();
                did.put(cv, nextValue);
                delays.put(cv, new DelayedAction(cv));
                return nextValue;
            } else {
                return did.get(cv);
            }
        }
    }

}
