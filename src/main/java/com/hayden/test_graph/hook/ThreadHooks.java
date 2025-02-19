package com.hayden.test_graph.hook;

import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.test_graph.thread.ResettableThreadLike;
import com.hayden.test_graph.thread.ResettableThreadScope;
import com.hayden.utilitymodule.proxies.ProxyUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import lombok.SneakyThrows;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.ProxyUtils;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadHooks {

    @Autowired
    ResettableThreadScope resettableThreadScope;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    List<ResettableThreadLike> resettables;

    static final ConcurrentHashMap<String, ThreadLocal<AtomicInteger>> i = new ConcurrentHashMap<>();

    static {
        i.put("key", new ThreadLocal<>());
    }

    @Before
    public void before() {
        i.compute("key", (key, prev) -> {
            Assert.notNull(prev, "Key was null, static initializer failed.");
            if (prev.get() == null)
                prev.set(new AtomicInteger(0));
            else {
                resettables.stream()
                        .sorted(Comparator.comparing(e -> e instanceof SubGraph<?,?> ? 1: -1))
                        .peek(ResettableThreadLike::preReset)
                        .forEach(r -> {
                            applicationContext.getAutowireCapableBeanFactory().autowireBean(r);
                        });
            }

            return prev;
        });
    }

}
