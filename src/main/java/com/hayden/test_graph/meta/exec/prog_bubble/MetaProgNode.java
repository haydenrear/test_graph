package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.HyperGraphNode;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.single.MetaNode;

import java.util.List;

public record MetaProgNode<T extends HyperGraphContext>(
        TestGraphNode<T> t,
        MetaCtx m,
        ProgCtxConverterComposite compositeConverter) implements MetaNode<T> {



    @Override
    public List<Class<? extends TestGraphNode<T>>> dependsOn() {
        return List.of();
    }

    @Override
    public List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of();
    }

    @Override
    public void exec(T exec) {
        t.exec(exec);
    }

    @Override
    public T mapCtx(T ctx) {
        m.put(t, ctx);
        return ctx;
    }

    @Override
    public void collectCtx() {
        m.collect();
    }
}
