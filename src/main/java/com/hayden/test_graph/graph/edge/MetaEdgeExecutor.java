package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * TODO??? Can be adjusted to make MetaCtx generic parameter also for better wiring for postExecMgMg
 * @param <T>
 * @param <H>
 * @param <M>
 */
@Slf4j
public class MetaEdgeExecutor<M extends MetaCtx> {

    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecMetaMetaGraphEdge<M>> postExecMgMg = new ArrayList<>();

//    public interface MetaGraphExec<M extends MetaCtx> extends HyperGraphExec<TestGraphContext<HyperGraphContext<MetaCtx>>, HyperGraphContext<M>, M> { }

//    public <HG_EXEC extends HyperGraphExec<TestGraphContext<HyperGraphContext<MetaCtx>>, HyperGraphContext<M>, M>> HG_EXEC preExecHgExecEdges(HG_EXEC exec, MetaCtx prev) {
//        return exec;
//    }

    public MetaCtx postExecMetaCtxEdges(MetaCtx prev, MetaCtx curr) {
//        if (curr instanceof MetaProgCtx mp) {
//            for (var m : retrieveMetaMeta(prev, curr, postExecMgMg)) {
//                prev = m.edge(prev, mp);
//            }
//        }
//
//        return prev;
        return null;
    }


}
