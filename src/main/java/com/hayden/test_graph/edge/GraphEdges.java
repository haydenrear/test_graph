package com.hayden.test_graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.stereotype.Component;

@Component
public class GraphEdges {

    public void addEdge(HyperGraphExec ctx, MetaCtx prev) {

    }

    public void addEdge(HyperGraphExec ctx, HyperGraphContext hgContext, MetaCtx prev) {

    }

    public void addEdge(GraphExec ctx, TestGraphContext tgc,  HyperGraphContext hgContext, MetaCtx prev) {

    }
}
