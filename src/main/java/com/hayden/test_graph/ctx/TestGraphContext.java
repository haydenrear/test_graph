package com.hayden.test_graph.ctx;

import com.google.common.collect.Lists;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.sort.GraphSort;

import java.util.*;

public non-sealed interface TestGraphContext<H extends HyperGraphContext>
        extends GraphContext,
                HierarchicalContext,
                GraphSort.GraphSortable {

    H bubble();

    Class<? extends H> bubbleClazz();

    boolean executableFor(GraphExec.GraphExecNode n);

    default boolean skip() {
        return false;
    }

    default Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.empty();
    }

    default List<? extends TestGraphContext<H>> parseContextTree() {
        List<? extends TestGraphContext<H>> tree = Lists.newArrayList(this);
        Optional.ofNullable(parent()).flatMap(p -> p.res().one().optional())
                .ifPresent(t -> tree.addAll(t.parseContextTree()));
        return tree;
    }

    default Collection<ContextValue<?>> requiredDependencies() {
        return new ArrayList<>() ;
    }

}