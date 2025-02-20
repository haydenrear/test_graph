package com.hayden.test_graph.commit_diff_context.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@Slf4j
@Component
public class DbDataSourceTrigger {

    public static final String APP_DB_KEY = "app_db_key";

    public static final String VALIDATION_DB_KEY = "validation_db_key";

    public interface SetKey {

        void setInit();

        void setInitialized();

        String starting();

    }


    private final ThreadLocal<String> threadKey = new ThreadLocal<>();

    private String currentKey = VALIDATION_DB_KEY;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    private final Object lock = new Object();

    public String setGlobalCurrentKey(String globalCurrent) {
        return doWithWriteLock(() -> {
            this.currentKey = globalCurrent;
        });
    }

    public String initializeGetKey() {
        if (countDownLatch.getCount() > 0) {
            synchronized (lock) {
                if (countDownLatch.getCount() > 0) {
                    return doWithWriteLock(() -> {
                        countDownLatch.countDown();
                        this.setInitializedInner();
                    });
                }
            }
        }

        return this.currentKey();
    }

    /**
     * Must be able to be accessed - one writer at a time.
     * @return
     */
    public String currentKey() {
        return Optional.ofNullable(threadKey.get())
                .orElseGet(() -> {
                    this.reentrantReadWriteLock.readLock().lock();
                    try {
                        return this.currentKey;
                    } finally {
                        this.reentrantReadWriteLock.readLock().unlock();
                    }
                });
    }

    public String doWithKey(Consumer<SetKey> setKeyConsumer) {
        String prev = currentKey;
        try {
            this.threadKey.set(currentKey);
            setKeyConsumer.accept(new SetKey() {
                @Override
                public void setInit() {
                    setValidationInner();
                }

                @Override
                public void setInitialized() {
                    setInitializedInner();
                }

                @Override
                public String starting() {
                    return prev;
                }
            });
            return this.threadKey.get();
        } finally {
            this.threadKey.remove();
        }
    }

    public String doWithWriteLock(Runnable toDo) {
        reentrantReadWriteLock.writeLock().lock();
        try {
            toDo.run();
            return this.currentKey;
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    private void setValidationInner() {
        this.threadKey.set(VALIDATION_DB_KEY);
    }

    private void setInitializedInner() {
        this.threadKey.set(APP_DB_KEY);
    }

}
