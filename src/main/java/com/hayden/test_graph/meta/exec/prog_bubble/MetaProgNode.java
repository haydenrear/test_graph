package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.node.HyperGraphNode;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.single.MetaNode;

import java.util.List;

public record MetaProgNode<T extends HyperGraphContext<MetaCtx>>(
            ContextValue<HyperGraphTestNode<T, MetaCtx>> t,
            ContextValue<MetaCtx> m
        )
        implements MetaNode {

    @Override
    public List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of();
    }

    @Override
    public List<Class<? extends TestGraphNode<MetaCtx, MetaCtx>>> dependsOn() {
        return List.of();
    }
}
