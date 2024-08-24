package com.hayden.test_graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.stereotype.Component;

@Component
public class GraphEdges {

    public <T extends HyperGraphExec> T addEdge(T ctx, MetaCtx prev) {
        return ctx;
    }

    public <T extends HyperGraphExec> T addEdge(T ctx, HyperGraphContext hgContext, MetaCtx prev) {
        return ctx;
    }

    public <T extends GraphExec> T addEdge(T exec, TestGraphContext tgc,  HyperGraphContext hgContext, MetaCtx prev) {
        return exec;
    }

    public <T extends GraphExec.ExecNode> T addEdge(T exec, TestGraphContext tgc,  MetaCtx prev) {
        return exec;
    }
}
