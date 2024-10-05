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
public class GraphEdges {

    // TODO: could inject the contexts for more flexibility.

    @Autowired(required = false)
    @ResettableThread
    private List<? extends HyperGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> fromEdges;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends MetaGraphEdge<? extends HyperGraphContext<MetaCtx>, MetaCtx>> metaEdges;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends MetaMetaGraphEdge<? extends MetaCtx>> metaMetaGraphEdges;
    @Autowired(required = false)
    @ResettableThread
    private List<? extends TestGraphEdge> testGraphEdges;

    @PostConstruct
    public void init() {
        if (fromEdges == null ) {
            fromEdges = new ArrayList<>();
        }
        if (metaEdges == null ) {
            metaEdges = new ArrayList<>();
        }
        if (metaMetaGraphEdges == null ) {
            metaMetaGraphEdges = new ArrayList<>();
        }
        if (testGraphEdges == null ) {
            testGraphEdges = new ArrayList<>();
        }
    }

    public <T extends HyperGraphExec> T preExecHgExecEdges(T exec, MetaCtx prev) {
        return exec;
    }

    public MetaCtx postExecMetaCtxEdges(MetaCtx prev, MetaCtx curr) {
        if (curr instanceof MetaProgCtx mp) {
            this.retrieveMetaMeta(prev, curr).forEach(m -> m.edge(prev, mp));
        }
        return prev;
    }

    public <T extends TestGraphContext> T preExecTestGraphEdges(T testGraphContext, MetaCtx prev) {
        this.retrieveFromEdges(testGraphContext, prev).forEach(he -> he.edge(testGraphContext, prev));
        return testGraphContext;
    }

    public <T extends HyperGraphExec, U extends HyperGraphContext<MetaCtx>> T postReducePreExecTestGraph(T hgExec, U hgContext, MetaCtx prev) {
        this.retrieveFromEdges(hgContext, prev).forEach(he -> he.edge(hgContext, prev));
        this.retrieveMetaEdges(hgContext, prev).forEach(hge -> hge.edge(prev, hgContext));
        return hgExec;
    }

    public <T extends HyperGraphExec, U extends HyperGraphContext<MetaCtx>> MetaCtx postExecHgEdges(T exec, U hgContext, MetaCtx prev, MetaCtx curr) {
        this.retrieveFromEdges(hgContext, curr).forEach(he -> he.edge(hgContext, curr));
        this.retrieveMetaEdges(hgContext, curr).forEach(hge -> hge.edge(curr, hgContext));
        return curr;
    }

    public <T extends HyperGraphContext<MetaCtx>> Stream<MetaGraphEdge<T, MetaCtx>> retrieveMetaEdges(T t, MetaCtx prev) {
        return metaEdges.stream().filter(hge -> hge.from().test(t) && hge.to().test(prev))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((MetaGraphEdge<T, MetaCtx>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}", c.getMessage());
                        return Stream.empty();
                    }
                });
    }

    public <T extends  MetaCtx> Stream<MetaMetaGraphEdge<T>> retrieveMetaMeta(T t, MetaCtx ctx) {
        return metaMetaGraphEdges.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((MetaMetaGraphEdge<T>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                });
    }

    public Stream<TestGraphEdge> retrieveFromEdges(TestGraphContext t, MetaCtx ctx) {
        return testGraphEdges.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((TestGraphEdge) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                });
    }

    public <T extends HyperGraphContext<MetaCtx>> Stream<HyperGraphEdge<T, MetaCtx>> retrieveFromEdges(T t, MetaCtx ctx) {
        return fromEdges.stream().filter(hge -> hge.from().test(t) && hge.to().test(ctx))
                .flatMap(hge -> {
                    try {
                        return Stream.ofNullable((HyperGraphEdge<T, MetaCtx>) hge);
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                });
    }

    public <T extends GraphExec> T edges(T exec, TestGraphContext tgc, HyperGraphContext hgContext, MetaCtx prev) {
        return exec;
    }

    public <T extends GraphExec.ExecNode> T edges(T exec, TestGraphContext tgc, MetaCtx prev) {
        return exec;
    }
}
