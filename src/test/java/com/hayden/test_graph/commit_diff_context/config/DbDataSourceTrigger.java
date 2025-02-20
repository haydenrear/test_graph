package com.hayden.test_graph.commit_diff_context.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
public class DbDataSourceTrigger {

    public interface SetKey {

        void setInit();

        void setInitialized();

        String starting();

    }

    private volatile String currentKey = "init";

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    private final Object lock = new Object();

    public void countDown() {
        if (countDownLatch.getCount() > 0) {
            synchronized (lock) {
                if (countDownLatch.getCount() > 0) {
                    doWithWriteLock(() -> {
                        countDownLatch.countDown();
                        this.setInitializedInner();
                    });
                }
            }
        }
    }

    /**
     * Must be able to be accessed - one writer at a time.
     * @return
     */
    public String currentKey() {
        this.reentrantReadWriteLock.readLock().lock();
        try {
            return this.currentKey;
        } finally {
            this.reentrantReadWriteLock.readLock().unlock();
        }
    }

    public void doWithKey(Consumer<SetKey> setKeyConsumer) {
        doWithWriteLock(() -> {
            String prev = currentKey;
            try {
                setKeyConsumer.accept(new SetKey() {
                    @Override
                    public void setInit() {
                        setInitInner();
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
            } finally {
                this.currentKey = prev;
            }
        });
    }

    public void doWithWriteLock(Runnable toDo) {
        reentrantReadWriteLock.writeLock().lock();
        try {
            toDo.run();
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    private void setInitInner() {
        this.currentKey = "init";
    }

    private void setInitializedInner() {
        this.currentKey = "initialized";
    }

}
