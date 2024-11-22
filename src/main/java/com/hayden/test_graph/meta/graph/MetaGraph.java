package com.hayden.test_graph.meta.graph;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ResettableThread
public class MetaGraph implements MetaHyperGraph {

    @Autowired
    TestGraphSort graphSort;
    @Autowired @Lazy
    LazyMetaGraphDelegate graphAutoDetect;

    private List<MetaProgNode<MetaProgCtx>> hyperGraphNodes;

    @Autowired
    @ResettableThread
    MetaCtx metaProgCtx;

    @Autowired
    @ResettableThread
    public void setHyperGraphNodes(List<HyperGraphTestNode> bubbleNodes) {
        this.hyperGraphNodes = graphSort.sort(bubbleNodes)
                .stream()
                .map(m -> new MetaProgNode<MetaProgCtx>(ContextValue.ofExisting(m), ContextValue.ofExisting(metaProgCtx)))
                .toList();
        log.info("Initialized {} hyper graph nodes.", this.hyperGraphNodes.size());
    }

    @Override
    public List<MetaCtx> bubble() {
        return List.of(metaProgCtx);
    }

    @Override
    public List<? extends MetaProgNode<MetaProgCtx>> sortedNodes() {
        return hyperGraphNodes;
    }


}
