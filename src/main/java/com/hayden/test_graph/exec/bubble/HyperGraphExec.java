package com.hayden.test_graph.exec.bubble;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface HyperGraphExec<SG extends TestGraphContext, CTX extends HyperGraphContext<H>, H extends HyperGraphContext<MetaCtx>>
        extends GraphExec.GraphExecNode<CTX, H>, HyperGraphTestNode<CTX, H> {

    /**
     * There exists some number of contexts that need to be collected into the bubble context. For example there could exist hierarchies
     * of contexts from HierarchicalContext - in this case all of these contexts needs to be then bubbled into a single hypergraph context.
     * So this exec is called for each of those contexts to produce a hypergraph context. These hypergraph contexts produced are then collected/merged
     *
     * @param ctx
     * @return
     */
    @Idempotent
    H exec(Class<? extends SG> ctx, MetaCtx prev);

    default H exec(Class<? extends SG> ctx) {
        return this.exec(ctx, null);
    }

    H collectCtx(CTX toCollect);

}
