package com.hayden.test_graph.meta.graph;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.graph.service.MetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ThreadScope
public class MetaGraph implements MetaHyperGraph<HyperGraphContext<MetaCtx>, MetaCtx> {

    @Autowired
    TestGraphSort graphSort;
    @Autowired @Lazy
    MetaGraphDelegate graphAutoDetect;

    private List<MetaProgNode<HyperGraphContext<MetaCtx>>> hyperGraphNodes;

    @Autowired
    @ThreadScope
    MetaCtx ctx;

    @Autowired
    @ThreadScope
    public void setHyperGraphNodes(List<HyperGraphTestNode> bubbleNodes) {
        this.hyperGraphNodes = graphSort.sort(bubbleNodes)
                .stream()
                .map(m -> new MetaProgNode<HyperGraphContext<MetaCtx>>(ContextValue.ofExisting(m), ContextValue.ofExisting(ctx)))
                .toList();
        log.info("Initialized {} hyper graph nodes.", this.hyperGraphNodes.size());
    }

    @Override
    public List<MetaCtx> bubble() {
        return List.of(ctx);
    }

    @Override
    public List<? extends MetaProgNode<HyperGraphContext<MetaCtx>>> sortedNodes() {
        return hyperGraphNodes;
    }


}
