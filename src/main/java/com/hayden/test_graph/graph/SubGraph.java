package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.service.GraphAutoDetect;
import com.hayden.test_graph.graph.service.TestGraphSort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <T>
 * @param <H>
 */
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
