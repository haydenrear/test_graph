package com.hayden.test_graph.graph.node;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface TestGraphNode<T extends TestGraphContext<H>, H extends HyperGraphContext> extends GraphNode,
        GraphExec.GraphExecNode<T> {

    /**
     * Graph nodes in a large part classified by the contexts of the nodes.
     * @return
     */
    Class<? extends T> clzz();

}
