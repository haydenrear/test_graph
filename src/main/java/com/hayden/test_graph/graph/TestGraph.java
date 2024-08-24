package com.hayden.test_graph.graph;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface TestGraph<T extends TestGraphContext<H>, H extends HyperGraphContext> extends Graph {

    List<? extends T> sortedCtx(Class<? extends T> clzz);

    Map<Class<? extends T>, List<? extends GraphNode<T, H>>> sortedNodes();

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

    private static <T extends TestGraphContext<H>, H extends HyperGraphContext> void setParentChild(Map.Entry<? extends T, ? extends T> e) {
        var i = e.getKey();
        var j = e.getValue();
        if (i instanceof HierarchicalContext.HasChildContext c
                && j instanceof HierarchicalContext.HasParentContext p) {
            if (p.toSet(c)) {
                p.doSet(c);
            }
        } else if (j instanceof HierarchicalContext.HasChildContext c
                && i instanceof HierarchicalContext.HasParentContext p) {
            if (p.toSet(c)) {
                p.doSet(c);
            }
        }
    }

    private static <T extends HierarchicalContext> boolean needsChildParent(T value) {
        return value.child().isEmpty() || value.parent().isEmpty();
    }

}
