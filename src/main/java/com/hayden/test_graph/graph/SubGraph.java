package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.thread.ResettableThreadLike;
import com.hayden.utilitymodule.sort.GraphSort;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @param <T>
 * @param <H>
 */
@Slf4j
@RequiredArgsConstructor
public class SubGraph<T extends TestGraphContext<H>, H extends HyperGraphContext>
        implements Graph, ApplicationContextAware, ResettableThreadLike, GraphSort.GraphSortable {

    @Setter
    @Getter
    private T t;


    private ApplicationContext ctx;

    public SubGraph(T t) {
        this.t = t;
    }

    public Class<? extends T> clazz() {
        return (Class<? extends T>) t.getClass();
    }

    public List<? extends T> parseContextTree() {
        return t.parseContextTree()
                .stream()
                .map(t -> (T) t)
                .toList();
    }

    @Override
    public <T extends GraphSort.GraphSortable> List<Class<? extends T>> dependsOn() {
        return this.t.bubble().dependsOn();
    }

    public List<TestGraphContext> dependsOnRecursive(Map<Class<? extends TestGraphContext>, TestGraphContext> graphCtxt) {
        var parsed = parseAllDepsNotThis(graphCtxt);
        return parsed.stream().distinct().toList();
    }

    public Class<? extends TestGraphContext> dependsOn(HyperGraphExec graphExec) {
        if(graphExec.clzz().isAssignableFrom(t.bubbleClazz())) {
            return this.clazz();
        }

        return null;
    }

    private static Optional<TestGraphContext> setParent(ApplicationContext beanFactory,
                                                        TestGraphContext i) {
        beanFactory.getAutowireCapableBeanFactory().autowireBean(i);
        return i.parentTy()
                .map(p -> {
                    TestGraphContext tgc = beanFactory.getBean((Class<? extends TestGraphContext>) p);
                    beanFactory.getAutowireCapableBeanFactory().autowireBean(tgc);
                    Assert.isTrue(i.parentTy().isPresent() && i.parentTy().get().equals(tgc.getClass()),
                            "Child type and parent type must be compatible for %s.".formatted(tgc.getClass().getName()));
                    i.doSet(tgc);
                    return tgc;
                });
    }

    @PostConstruct
    public void setParent() {
        // have to do this because of resettable ThreadScope
        this.preReset();
        Optional<TestGraphContext> next = Optional.ofNullable(this.t);
        while (next.isPresent()) {
            log.info("Setting parent: {} for subgraph {}.", next.get(), this.t);
            next = setParent(ctx, next.get());
        }
    }

    @Override
    public void preReset() {
        this.t = ctx.getBean(this.clazz());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
