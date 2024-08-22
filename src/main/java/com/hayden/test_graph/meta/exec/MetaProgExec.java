package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.TestGraph;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import com.hayden.test_graph.meta.graph.MetaGraph;

import java.util.List;
import java.util.function.Predicate;

public class MetaProgExec implements ProgExec<MetaProgNode<HyperGraphContext>> {


    @Override
    public MetaGraph graph() {
        return null;
    }

    @Override
    public ProgExec<MetaProgNode<HyperGraphContext>> map(ProgExecNode<MetaCtx> toExec) {
        return null;
    }

    @Override
    public ProgExec<MetaProgNode<HyperGraphContext>> filter(Predicate<GraphExecNode<MetaCtx>> toExec) {
        return null;
    }

    @Override
    public ProgExec<MetaProgNode<HyperGraphContext>> collect() {
        return null;
    }

    @Override
    public GraphExec<MetaCtx, MetaCtx> reduce(GraphExecReducer<MetaCtx, MetaCtx> reducer) {
        return null;
    }

    @Override
    public ProgExec<MetaProgNode<HyperGraphContext>> reduce(ProgExecReducer<MetaProgNode<HyperGraphContext>> reducer) {
        return null;
    }

    @Override
    public void exec(MetaCtx ctx) {

    }

    @Override
    public List<GraphExecReducer<MetaCtx, MetaCtx>> reducers() {
        return List.of();
    }

    @Override
    public GraphExec<MetaCtx, MetaCtx> map(GraphExecNode<MetaCtx> toExec) {
        return null;
    }


}
