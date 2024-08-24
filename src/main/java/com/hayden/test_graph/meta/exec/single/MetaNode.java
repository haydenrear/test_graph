package com.hayden.test_graph.meta.exec.single;

import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.graph.node.HyperGraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface MetaNode extends HyperGraphNode<MetaCtx, MetaCtx>, ProgExec.ProgExecNode<MetaCtx> {


}
