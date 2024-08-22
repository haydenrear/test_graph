package com.hayden.test_graph.exec.single;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.Graph;

import java.util.List;
import java.util.function.Predicate;

public interface GraphExec<CTX extends TestGraphContext<H>, H extends HyperGraphContext> {

    interface GraphExecNode<CTX extends TestGraphContext> {
        void exec(CTX ctx);
    }

    interface GraphExecReducer<CTX extends TestGraphContext<H>, H extends HyperGraphContext> {
        GraphExec<CTX, H> reduce(GraphExecNode<CTX> first, GraphExecNode<CTX> second);
    }

    Graph<CTX,H> graph();

    void exec(CTX ctx);

    List<GraphExecReducer<CTX, H>> reducers();

    GraphExec<CTX, H> map(GraphExecNode<CTX> toExec);

    GraphExec<CTX, H> filter(Predicate<GraphExecNode<CTX>> toExec);

    GraphExec<CTX, H> collect();

    GraphExec<CTX, H> reduce(GraphExecReducer<CTX, H> reducer);

}
