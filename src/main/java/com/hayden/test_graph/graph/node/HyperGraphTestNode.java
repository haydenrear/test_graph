package com.hayden.test_graph.graph.node;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface HyperGraphTestNode<T extends HyperGraphContext<H>, H extends HyperGraphContext<MetaCtx>>
        extends HyperGraphBubbleNode<T, H> {


}
