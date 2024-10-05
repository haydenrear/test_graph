package com.hayden.test_graph.hook;

import com.hayden.test_graph.thread.ResettableThreadLike;
import com.hayden.test_graph.thread.ResettableThreadScope;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadHooks {

    @Autowired
    ResettableThreadScope resettableThreadScope;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    List<ResettableThreadLike> initializeAspect;

    private static final AtomicInteger i = new AtomicInteger(0);

    @Before
    public void before() {
        if (i.getAndIncrement() != 0) {
            resettableThreadScope.reset();
            initializeAspect.forEach(r -> applicationContext.getAutowireCapableBeanFactory().autowireBean(r));
        }
    }

}
