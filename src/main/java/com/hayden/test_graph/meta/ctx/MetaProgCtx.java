package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.meta.exec.prog_bubble.ProgCtxConverterComposite;

import java.util.List;

public class MetaProgCtx implements MetaCtx {
    @Override
    public List<TestGraphContext> ctx() {
        return List.of();
    }

    @Override
    public List<CompositeNodeMap> compositeNodes() {
        return List.of();
    }

    @Override
    public ProgCtxConverterComposite compositeConverter() {
        return null;
    }

    @Override
    public void put(TestGraphNode node, TestGraphContext ctx) {

    }

    @Override
    public MetaCtx bubble() {
        return null;
    }
}
