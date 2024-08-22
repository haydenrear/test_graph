package com.hayden.test_graph.exec.bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.edge.HyperGraphEdge;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.HyperGraphTestNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface HyperGraphExec<SG extends TestGraphContext<CTX>, CTX extends HyperGraphContext<H>, H extends HyperGraphContext<H>> extends GraphExec<CTX, H>, HyperGraphTestNode<CTX, H> {

    Logger log = LoggerFactory.getLogger(HyperGraphExec.class);

    /**
     * There exists some number of contexts that need to be collected into the bubble context. For example there could exist hierarchies
     * of contexts from HierarchicalContext - in this case all of these contexts needs to be then bubbled into a single hypergraph context.
     * So this exec is called for each of those contexts to produce a hypergraph context. These hypergraph contexts produced are then collected/merged
     *
     * @param ctx
     * @return
     */
    H exec(Class<? extends SG> ctx, MetaCtx prev);

    default H exec(Class<? extends SG> ctx) {
        return this.exec(ctx, null);
    }

    H collectCtx(CTX toCollect);

    default void visit(HyperGraphEdge<HyperGraphContext<MetaCtx>, MetaCtx> edge) {

    }

}
