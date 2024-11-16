package com.hayden.test_graph.ctx;

import com.google.common.collect.Lists;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.sort.GraphSort;

import java.util.*;

public non-sealed interface TestGraphContext<H extends HyperGraphContext<MetaCtx>>
        extends GraphContext,
                HierarchicalContext,
                GraphSort.GraphSortable {

    H bubble();

    Class<? extends H> bubbleClazz();

    boolean executableFor(GraphExec.GraphExecNode n);

    default Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.empty();
    }

    default List<? extends TestGraphContext<H>> parseContextTree() {
        List<? extends TestGraphContext<H>> tree = Lists.newArrayList(this);
        Optional.ofNullable(parent()).flatMap(p -> p.res().optional())
                .ifPresent(t -> tree.addAll(t.parseContextTree()));
        return tree;
    }

    default List<Class<? extends TestGraphContext>> dependsOnRecursive() {
        List<Class<? extends TestGraphContext>> d = new ArrayList<>(this.dependsOn());
        List<Class<? extends TestGraphContext>> parentDep = this.bubble().dependsOn();
        d.addAll(parentDep) ;

        return d.stream().distinct().toList();
    }

    default Collection<ContextValue<?>> requiredDependencies() {
        return new ArrayList<>() ;
    }

}