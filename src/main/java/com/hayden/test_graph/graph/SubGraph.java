package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SubGraph<T extends TestGraphContext<H>, H extends HyperGraphContext> implements Graph<H> {

    private final T t;

    public Class<? extends T> clazz() {
        return (Class<? extends T>) t.getClass();
    }

    public List<? extends T> parseContextTree() {
        List<T> l = new ArrayList<>();
        var n = t;
        l.add(t);
        while (n != null && n.parent().res().isPresent()) {
            n = (T) n.parent().res().r().get();
            l.add(n);
        }
        return l;
    }

    @Override
    public TestGraphSort sortingAlgorithm() {
        return null;
    }

    @Override
    public GraphAutoDetect allNodes() {
        return null;
    }
}
