package com.hayden.test_graph.meta.graph;

import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.single.MetaNode;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class MetaGraph implements HyperGraph<MetaCtx, MetaCtx, MetaNode> {

    @Autowired
    @ThreadScope
    MetaCtx ctx;

    @Autowired
    TestGraphSort graphSort;

    List<HyperGraphNode> hyperGraphNodes;

    @Autowired
    @ThreadScope
    public void setHyperGraphNodes(List<HyperGraphNode> hyperGraphNodes) {
        this.hyperGraphNodes = graphSort.toGraphSortable(hyperGraphNodes);
    }

    @Override
    public MetaGraph fromSorted(List<MetaNode> testGraphNodes) {
        return null;
    }

    @Override
    public List<HyperGraphNode<MetaCtx>> sortedNodes() {
        return List.of();
    }

    @Override
    public GraphAutoDetect allNodes() {
        return null;
    }


    public List<MetaCtx> ctx() {
        return List.of(ctx);
    }

    @Override
    public List<MetaCtx> forBubbling() {
        return List.of();
    }
}
