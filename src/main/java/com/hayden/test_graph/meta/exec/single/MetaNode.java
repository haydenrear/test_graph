package com.hayden.test_graph.meta.exec.single;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.graph.HyperGraphNode;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface MetaNode<OTHER extends HyperGraphContext<MetaCtx>> extends HyperGraphNode<MetaCtx, MetaCtx>, ProgExec.ProgExecNode<MetaCtx> {


}
