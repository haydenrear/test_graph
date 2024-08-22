package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;

public interface MetaCtx extends HyperGraphContext, TestGraphContext<MetaCtx> {


    default void collect() {
        compositeNodes().stream()
                .map(c -> ((CompositeNodeMap) c).contextComposite())
                .distinct()
                .forEach(t -> compositeConverter().visit((HyperGraphContext) t, this));
    }
}
