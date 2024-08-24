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
        var arrList = new ArrayList<T>();
        var nextValue = t;
        arrList.add(t);

        while (nextValue != null && nextValue.parent().res().isPresent()) {
            nextValue = (T) nextValue.parent().res().r().get();
            arrList.add(nextValue);
        }

        return arrList;
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
