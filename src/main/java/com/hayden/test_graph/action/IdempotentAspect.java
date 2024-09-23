package com.hayden.test_graph.action;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Aspect
@Component
@Slf4j
public class IdempotentAspect implements ApplicationContextAware {

    private final ConcurrentHashMap<CacheValue, DelayedAction> delays = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends DoRunAgain>, DoRunAgain> runAgain = new ConcurrentHashMap<>();

    private ApplicationContext ctx;

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    record CacheValue(Object method, Object target) { }

    record DelayedAction(LocalDateTime expireFutureTime, LocalDateTime now, CacheValue cacheValue, Object ret) implements Delayed {

        DelayedAction(LocalDateTime expireFutureTime, CacheValue cacheValue, Object ret) {
            this(expireFutureTime, LocalDateTime.now(), cacheValue, ret);
        }

        DelayedAction(CacheValue cacheValue, Object ret) {
            this(LocalDateTime.MAX, LocalDateTime.now(), cacheValue, ret);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            if (o instanceof DelayedAction d) {
                return this.expireFutureTime.compareTo(d.expireFutureTime);
            } else {
                throw new RuntimeException();
            }
        }

        public boolean isExpired() {
            return getDelay(TimeUnit.MILLISECONDS) <= 0;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(Duration.between(LocalDateTime.now(), expireFutureTime));
        }
    }

    private static final Object voidObj = new Object();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        if (isRunAgainBehavior(idempotent) && isTimed(idempotent))
            throw new UnsupportedOperationException("A run again class and a timer were added. Can only use one or the other.");

        addRunAgain(idempotent);

        this.delays.entrySet().stream()
                .filter(d -> d.getValue().isExpired())
                .map(Map.Entry::getKey)
                .toList()
                .forEach(this.delays::remove);

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

    private void addRunAgain(Idempotent idempotent) {
        if (!isNotRunAgainBehavior(idempotent))  {
            runAgain.computeIfAbsent(idempotent.runAgain(), k -> {
                try {
                    var created = idempotent.runAgain().getDeclaredConstructor().newInstance();
                    this.ctx.getAutowireCapableBeanFactory().autowireBean(created);
                    return created;
                } catch (
                        InstantiationException |
                        IllegalAccessException |
                        InvocationTargetException |
                        NoSuchMethodException e) {
                    log.error("Error when creating new instance of DoRunAgain, {}. Will not be tried again", idempotent.runAgain(), e);
                    return null;
                }
            });
        }
    }

    private DelayedAction callIfNotExpiredOrNotCached(ProceedingJoinPoint joinPoint, CacheValue cv, Idempotent idempotent, DelayedAction prev) {
        if (isNotReRunnable(idempotent)) {
            if (prev == voidObj) {
                return prev;
            } else if (prev == null) {
                return getProceedToAdd(joinPoint, cv);
            } else {
                Assert.notNull(prev, "Assumed not to be null.");
                return idempotentPrev(joinPoint, idempotent, prev);
            }
        }


        return doRerun(joinPoint, idempotent, prev, cv);
    }

    private DelayedAction doRerun(ProceedingJoinPoint joinPoint, Idempotent idempotent, DelayedAction prev, CacheValue cv) {
        if(prev.getDelay(TimeUnit.MILLISECONDS) <= 0) {
            return getProceedToAddTimed(joinPoint, cv, idempotent);
        } else if (isNotRunAgainBehavior(idempotent)) {
            return prev;
        } else {
            AtomicReference<DelayedAction> p = new AtomicReference<>(prev);
            this.runAgain.compute(idempotent.runAgain(), (key, prevRun) -> {
                Assert.notNull(prevRun, "Assumed not to be null.");
                if (prevRun.doRunAgain(joinPoint.getArgs())) {
                    p.set(getProceedToAdd(joinPoint, cv));
                    return prevRun;
                } else {
                    return prevRun;
                }
            });

            return p.get();
        }
    }

    @SneakyThrows
    private DelayedAction getProceedToAdd(ProceedingJoinPoint joinPoint, CacheValue cv) {
        return Optional.ofNullable(joinPoint.proceed())
                .or(() -> Optional.of(voidObj))
                .map(o -> new DelayedAction(cv, o))
                .get();
    }

    @SneakyThrows
    private DelayedAction getProceedToAddTimed(ProceedingJoinPoint joinPoint, CacheValue cv, Idempotent idempotent) {
        return Optional.ofNullable(joinPoint.proceed())
                .or(() -> Optional.of(voidObj))
                .map(o -> new DelayedAction(LocalDateTime.now().plus(idempotent.timeoutMillis(), ChronoUnit.MILLIS), cv, o))
                .get();
    }

    private static DelayedAction idempotentPrev(ProceedingJoinPoint joinPoint, Idempotent idempotent, DelayedAction prev) {
        int numArgs = Optional.ofNullable(joinPoint.getArgs())
                .map(a -> a.length)
                .orElse(-1);

        if(idempotent.returnArg() == -1 || numArgs == -1)
            return prev;
        else {
            if (numArgs <= idempotent.returnArg()) {
                String message = "Return index provided was greater than argument.";
                log.error(message);
                return prev;
            }

            Object retArg = joinPoint.getArgs()[idempotent.returnArg()];

            if (joinPoint instanceof MethodInvocationProceedingJoinPoint j
                    && j.getSignature() instanceof MethodSignature m
                    && !m.getReturnType().isAssignableFrom(retArg.getClass())) {
                log.error("Return type did not match arg index for {}, {}, {}", joinPoint.getSignature(), retArg.getClass(), m.getReturnType());
                return prev;
            }

            return new DelayedAction(prev.expireFutureTime, prev.cacheValue, retArg);
        }
    }

    private boolean isNotReRunnable(Idempotent idempotent) {
        return isNotTimed(idempotent) && isNotRunAgainBehavior(idempotent);
    }

    private static boolean isRunAgainBehavior(Idempotent idempotent) {
        return !isNotRunAgainBehavior(idempotent) ;
    }

    private static boolean isNotRunAgainBehavior(Idempotent idempotent) {
        return idempotent.runAgain().equals(DoRunAgain.class);
    }

    private boolean isTimed(Idempotent idempotent) {
        return idempotent.timeoutMillis() > 0L;
    }

    private boolean isNotTimed(Idempotent idempotent) {
        return !isTimed(idempotent) ;
    }
}
