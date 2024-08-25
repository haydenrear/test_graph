package com.hayden.test_graph.ctx;

import com.google.common.collect.Lists;
import com.hayden.test_graph.graph.TestGraph;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.utilitymodule.sort.GraphSort;

import java.util.List;
import java.util.Optional;

public non-sealed interface TestGraphContext<H extends HyperGraphContext>
        extends GraphContext<TestGraphContext<H>>,
                HierarchicalContext,
                GraphSort.GraphSortable<TestGraphContext<H>> {

    H bubble();

    boolean executableFor(GraphNode n);

    default Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.empty();
    }

    default Optional<Class<? extends TestGraphContext>> childTy() {
        return Optional.empty();
    }

    default List<? extends TestGraphContext<H>> parseContextTree() {
        List<? extends TestGraphContext<H>> tree = Lists.newArrayList(this);
        parent().res().ifPresent(t -> tree.addAll(t.parseContextTree()));
        return tree;
    }

}