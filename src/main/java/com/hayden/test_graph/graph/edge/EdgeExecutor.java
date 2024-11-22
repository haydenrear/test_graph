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
public class EdgeExecutor<T extends TestGraphContext<H>, H extends HyperGraphContext<MetaCtx>, M extends MetaCtx> {

    @Autowired(required = false)
    @ResettableThread
    private List<? extends PreExecTestGraphEdge<T, H>> preExecTg = new ArrayList<>();
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostReduceHyperGraphEdge<H, MetaCtx>> postReducePreExecHg = new ArrayList<>();
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecHyperGraphEdge<H, MetaCtx>> postExecHg = new ArrayList<>();
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecMetaGraphEdge<H, MetaCtx>> postExecMg = new ArrayList<>();
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostReduceMetaGraphEdge<H, MetaCtx>> postReducePreExecMg = new ArrayList<>();
    @Autowired(required = false)
    @ResettableThread
    private List<? extends PostExecMetaMetaGraphEdge<M>> postExecMgMg = new ArrayList<>();


    public <HG_EXEC extends HyperGraphExec<T, H>> HG_EXEC preExecHgExecEdges(HG_EXEC exec, MetaCtx prev) {
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

    public H preExecTestGraphEdges(H testGraphContext, MetaCtx prev) {
        for (TestGraphEdge<H, ? extends HyperGraphContext<MetaCtx>> he : this.retrieveFromEdges(testGraphContext, prev, this.preExecTg)) {
            testGraphContext = he.edge(testGraphContext, prev);
        }

        return testGraphContext;
    }

    public <HG_EXEC extends HyperGraphExec> MetaCtx postReducePreExecMetaCtx(HG_EXEC hgExec, H hgContext, MetaCtx prev) {
        for (MetaGraphEdge<H, MetaCtx> he : this.retrieveMetaEdges(hgContext, prev, this.postReducePreExecMg)) {
            prev = he.edge(prev, hgContext);
        }

        return prev;
    }

    public <HG_EXEC extends HyperGraphExec> HG_EXEC postReducePreExecTestGraph(HG_EXEC hgExec, H hgContext, MetaCtx prev) {
        for (HyperGraphEdge<H, MetaCtx> he : this.retrieveFromEdges(hgContext, prev, this.postReducePreExecHg)) {
            hgContext = he.edge(hgContext, prev);
        }

        return hgExec;
    }

    public <HG_EXEC extends HyperGraphExec> MetaCtx postExecHgEdges(HG_EXEC exec, H hgContext, MetaCtx prev, MetaCtx curr) {
        for (HyperGraphEdge<H, MetaCtx> he : this.retrieveFromEdges(hgContext, curr, this.postExecHg)) {
            hgContext = he.edge(hgContext, curr);
        }
        for (MetaGraphEdge<H, MetaCtx> he : this.retrieveMetaEdges(hgContext, curr, this.postExecMg)) {
            curr = he.edge(curr, hgContext);
        }

        return curr;
    }

    public <HG_EXEC extends HyperGraphContext<MetaCtx>> List<MetaGraphEdge<HG_EXEC, MetaCtx>> retrieveMetaEdges(
            HG_EXEC t, MetaCtx prev,
            List<? extends MetaGraphEdge<H, MetaCtx>> mg
    ) {
        return mg.stream()
                .filter(hge -> hge.from().test(t) && hge.to().test(prev))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((MetaGraphEdge<HG_EXEC, MetaCtx>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}", c.getMessage());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public <HG_EXEC extends  MetaCtx> List<MetaMetaGraphEdge<HG_EXEC>> retrieveMetaMeta(HG_EXEC t, MetaCtx ctx, List<? extends MetaMetaGraphEdge<? extends MetaCtx>> mm) {
        return mm.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((MetaMetaGraphEdge<HG_EXEC>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public List<TestGraphEdge> retrieveFromEdges(TestGraphContext t, MetaCtx ctx, List<? extends PreExecTestGraphEdge<T, H>> tg) {
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

    public List<HyperGraphEdge<H, MetaCtx>> retrieveFromEdges(H t, MetaCtx ctx, List<? extends HyperGraphEdge<H, MetaCtx>> hg) {
        return hg.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((HyperGraphEdge<H, MetaCtx>) hge);
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
