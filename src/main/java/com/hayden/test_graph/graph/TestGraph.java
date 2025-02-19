package com.hayden.test_graph.graph;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public interface TestGraph<T extends TestGraphContext<H>, H extends HyperGraphContext> extends Graph {

    Logger log = LoggerFactory.getLogger(TestGraph.class);

    List<? extends T> sortedCtx(Class<? extends T> clzz);

    Map<Class<? extends T>, List<GraphExec.GraphExecNode<T>>> sortedNodes();

    default List<GraphExec.GraphExecNode<T>> toRunSortedNodes(T init) {
        return Optional.ofNullable(sortedNodes().get(init.getClass())).orElse(new ArrayList<>())
                .stream()
                .filter(ge -> {
                    if (ge.skip(init)) {
                        log.info("Skipping {}.", ge.getClass().getName());
                        return false;
                    }

                    return true;
                })
                .toList();

    }

    default void setChildren() {
        sortedNodes().keySet().forEach(ctx -> setChildren(() -> sortedCtx(ctx)));
    }


    @SneakyThrows
    @Idempotent
    default void setChildren(Callable<? extends List<? extends T>> ts) {
        List<? extends T> call = ts.call();
        call.stream().flatMap(i -> call.stream().map(j -> Map.entry(i, j)))
                .filter(e -> e.getKey() != e.getValue())
                .filter(e -> needsChildParent(e.getValue()) || needsChildParent(e.getKey()))
                .forEach(TestGraph::setParentChild);
    }

    private static <T extends TestGraphContext<H>, H extends HyperGraphContext<MetaCtx>> void setParentChild(Map.Entry<? extends T, ? extends T> e) {
        var i = e.getKey();
        var j = e.getValue();
        if (i instanceof TestGraphContext c
                && j instanceof HierarchicalContext.HasParentContext p) {
            if (p.toSet(c)) {
                p.doSet(c);
            }
        } else if (j instanceof TestGraphContext c
                && i instanceof HierarchicalContext.HasParentContext p) {
            if (p.toSet(c)) {
                p.doSet(c);
            }
        }
    }

    private static <T extends HierarchicalContext> boolean needsChildParent(T value) {
        return value.parent().isEmpty();
    }

}
