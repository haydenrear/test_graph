package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
public class SubGraph<T extends TestGraphContext<H>, H extends HyperGraphContext<MetaCtx>> implements Graph, ApplicationContextAware {

    private final T t;

    private final TestGraphSort graphSort;

    private ApplicationContext ctx;

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

    private static Optional<TestGraphContext> setParentChild(ApplicationContext beanFactory,
                                                             TestGraphContext i,
                                                             TestGraphContext prev) {
        beanFactory.getAutowireCapableBeanFactory().autowireBean(i);
        i.childTy().ifPresentOrElse(
                p -> {
                    Assert.isTrue(p == prev.getClass(), "Previous parent must be equal to child for %s, %s."
                            .formatted(p, prev.getClass()));
                    i.child().set(prev);
                },
                () -> Assert.isNull(prev, "Child must be null if doesn't have parent for %s.".formatted(i.getClass()))
        );
        return i.parentTy()
                .map(p -> {
                    var tgc = beanFactory.getBean((Class<? extends TestGraphContext>) p);
                    beanFactory.getAutowireCapableBeanFactory().autowireBean(tgc);
                    Assert.isTrue(tgc.childTy().isPresent() && tgc.childTy().get().equals(i.getClass()),
                            "Child type and parent type must be compatible for %s.".formatted(tgc.getClass().getName()));
                    i.parent().set(tgc);
                    return tgc;
                });
    }

    @PostConstruct
    public void setParentChild() {
        Optional<TestGraphContext> next = Optional.ofNullable(this.t);
        TestGraphContext prev = null;
        while (next.isPresent()) {
            var nextValue = next.get();
            next = setParentChild(ctx, next.get(), prev);
            prev = nextValue;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
