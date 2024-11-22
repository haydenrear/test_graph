package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.test_graph.thread.ResettableThreadLike;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 *
 * @param <T>
 * @param <H>
 */
@RequiredArgsConstructor
public class SubGraph<T extends TestGraphContext<H>, H extends HyperGraphContext>
        implements Graph, ApplicationContextAware, ResettableThreadLike {

    @Setter
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

    public Class<? extends TestGraphContext> dependsOn(HyperGraphExec graphExec) {
        if(graphExec.clzz().isAssignableFrom(t.bubbleClazz())){
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
        TestGraphContext prev = null;
        while (next.isPresent()) {
            var nextValue = next.get();
            next = setParent(ctx, next.get());
            prev = nextValue;
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
