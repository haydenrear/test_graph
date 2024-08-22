package com.hayden.test_graph.exec.prog_bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.HyperGraph;
import com.hayden.test_graph.graph.TestGraph;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.single.MetaNode;

import java.util.function.Predicate;

public interface ProgExec<N extends MetaNode<HyperGraphContext>> extends GraphExec<MetaCtx, MetaCtx> {

    interface ProgExecNode<CTX extends HyperGraphContext> extends GraphExec.GraphExecNode<CTX> { }

    interface ProgExecReducer<N extends MetaNode<HyperGraphContext>> extends GraphExec.GraphExecReducer<TestGraphContext<MetaCtx>, MetaCtx> {
        ProgExec<N> reduce(ProgExecNode<MetaCtx> first, ProgExecNode<MetaCtx> second);
    }


    ProgExec<N> map(ProgExecNode<MetaCtx> toExec);

    ProgExec<N> filter(Predicate<GraphExecNode<MetaCtx>> toExec);

    ProgExec<N> collect();

    ProgExec<N> reduce(ProgExecReducer<N> reducer);

    void exec(MetaCtx ctx);

}
