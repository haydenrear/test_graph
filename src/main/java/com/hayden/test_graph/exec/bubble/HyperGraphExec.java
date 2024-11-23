package com.hayden.test_graph.exec.bubble;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.ArrayList;
import java.util.List;

// TODO: abstract base class for the fields?
public interface HyperGraphExec<SG extends TestGraphContext<CTX>, CTX extends HyperGraphContext>
        extends HyperGraphTestNode<CTX> {

    /**
     * There exists some number of contexts that need to be collected into the bubble context. For example there could exist hierarchies
     * of contexts from HierarchicalContext - in this case all of these contexts needs to be then bubbled into a single hypergraph context.
     * So this exec is called for each of those contexts to produce a hypergraph context. These hypergraph contexts produced are then collected/merged
     *
     * @param ctx
     * @return
     */
    @Idempotent
    CTX exec(Class<? extends SG> ctx, MetaCtx prev);

    @Idempotent
    default MetaCtx execBubble(Class<? extends SG> ctx, MetaCtx prev) {
        return this.exec(ctx, prev).bubbleMeta(prev);
    }

    default CTX exec(Class<? extends SG> ctx) {
        return this.exec(ctx, null);
    }

    default CTX collectCtx(CTX toCollect) {
        return toCollect;
    }

    List<? extends HyperGraphBubbleNode<CTX>> sortedNodes();


    @Override
    default CTX preMap(CTX ctx, MetaCtx metaCtx) {
        for (var r : preMappers()) {
            var c = r.apply(ctx, metaCtx);
            ctx = c;
        }
        return ctx;
    }

    @Override
    default CTX postMap(CTX ctx, MetaCtx metaCtx) {
        for (var r : postMappers()) {
            ctx = r.apply(ctx, metaCtx);
        }
        for (var b : sortedNodes()) {
            ctx = b.preMap(ctx, metaCtx);
        }
        return ctx;
    }


}
