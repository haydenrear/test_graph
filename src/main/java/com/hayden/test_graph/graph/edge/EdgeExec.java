package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ResettableThread;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class EdgeExec {

    // TODO: could inject the contexts for more flexibility.

    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostReduceHyperGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> postReducePreExecHg;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecHyperGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> postExecHg;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecMetaGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> postExecMg;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostReduceMetaGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> postReducePreExecMg;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecMetaMetaGraphEdge<? extends MetaCtx>> postExecMgMg;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PreExecTestGraphEdge> preExecTg;

    @PostConstruct
    public void init() {
        if (postExecHg == null) {
            postExecHg = new ArrayList<>();
        }
        if (postExecMg == null) {
            postExecMg = new ArrayList<>();
        }
        if (postExecMgMg == null) {
            postExecMgMg = new ArrayList<>();
        }
        if (preExecTg == null) {
            preExecTg = new ArrayList<>();
        }
        if (postReducePreExecHg == null) {
            postReducePreExecHg = new ArrayList<>();
        }
        if (postReducePreExecMg == null) {
            postReducePreExecMg = new ArrayList<>();
        }
    }

    public <T extends HyperGraphExec<TestGraphContext, HyperGraphContext<MetaCtx>, MetaCtx>> T preExecHgExecEdges(T exec, MetaCtx prev) {
        return exec;
    }

    public MetaCtx postExecMetaCtxEdges(MetaCtx prev, MetaCtx curr) {
        if (curr instanceof MetaProgCtx mp) {
            for (var m : retrieveMetaMeta(prev, curr, postExecMgMg)) {
                prev = m.edge(prev, mp);
            }
        }

        return prev;
    }

    public <T extends TestGraphContext<? extends HyperGraphContext<MetaCtx>>> T preExecTestGraphEdges(T testGraphContext, MetaCtx prev) {
        for (TestGraphEdge<T, ? extends HyperGraphContext<MetaCtx>> he : this.retrieveFromEdges(testGraphContext, prev, this.preExecTg)) {
            testGraphContext = he.edge(testGraphContext, prev);
        }

        return testGraphContext;
    }

    public <T extends HyperGraphExec, U extends HyperGraphContext<MetaCtx>> MetaCtx postReducePreExecMetaCtx(T hgExec, U hgContext, MetaCtx prev) {
        for (MetaGraphEdge<U, MetaCtx> he : this.retrieveMetaEdges(hgContext, prev, this.postReducePreExecMg)) {
            prev = he.edge(prev, hgContext);
        }

        return prev;
    }

    public <T extends HyperGraphExec, U extends HyperGraphContext<MetaCtx>> T postReducePreExecTestGraph(T hgExec, U hgContext, MetaCtx prev) {
        for (HyperGraphEdge<U, MetaCtx> he : this.retrieveFromEdges(hgContext, prev, this.postReducePreExecHg)) {
            hgContext = he.edge(hgContext, prev);
        }

        return hgExec;
    }


    public <T extends HyperGraphExec, U extends HyperGraphContext<MetaCtx>> MetaCtx postExecHgEdges(T exec, U hgContext, MetaCtx prev, MetaCtx curr) {
        for (HyperGraphEdge<U, MetaCtx> he : this.retrieveFromEdges(hgContext, curr, this.postExecHg)) {
            hgContext = he.edge(hgContext, curr);
        }
        for (MetaGraphEdge<U, MetaCtx> he : this.retrieveMetaEdges(hgContext, curr, this.postExecMg)) {
            curr = he.edge(curr, hgContext);
        }

        return curr;
    }

    public <T extends HyperGraphContext<MetaCtx>> List<MetaGraphEdge<T, MetaCtx>> retrieveMetaEdges(
            T t, MetaCtx prev,
            List<? extends MetaGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> mg
    ) {
        return mg.stream().filter(hge -> hge.from().test(t) && hge.to().test(prev))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((MetaGraphEdge<T, MetaCtx>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}", c.getMessage());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public <T extends  MetaCtx> List<MetaMetaGraphEdge<T>> retrieveMetaMeta(T t, MetaCtx ctx, List<? extends MetaMetaGraphEdge<? extends MetaCtx>> mm) {
        return mm.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((MetaMetaGraphEdge<T>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public List<TestGraphEdge> retrieveFromEdges(TestGraphContext t, MetaCtx ctx, List<? extends PreExecTestGraphEdge> tg) {
        return tg.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((TestGraphEdge) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public <T extends HyperGraphContext<MetaCtx>> List<HyperGraphEdge<T, MetaCtx>> retrieveFromEdges(T t, MetaCtx ctx, List<? extends HyperGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> hg) {
        return hg.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((HyperGraphEdge<T, MetaCtx>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public <T extends GraphExec> T edges(T exec, TestGraphContext tgc, HyperGraphContext hgContext, MetaCtx prev) {
        return exec;
    }

    public <T extends GraphExec.ExecNode> T edges(T exec, TestGraphContext tgc, MetaCtx prev) {
        return exec;
    }
}
