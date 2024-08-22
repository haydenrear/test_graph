package com.hayden.test_graph.ctx;

import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.ProgCtxConverterComposite;

import java.util.List;
import java.util.Optional;

public interface HyperGraphContext extends TestGraphContext<MetaCtx> {

    List<TestGraphContext> ctx();

    default <T extends TestGraphContext> Optional<T> retrieveCtx(Class<T> cls) {
        return Optional.empty();
    }

    record CompositeNodeMap(TestGraphNode<HyperGraphContext> node, HyperGraphContext contextComposite) {}

    List<CompositeNodeMap> compositeNodes();

    <T extends TestGraphContext> void put(TestGraphNode<T> node, T ctx);

    ProgCtxConverterComposite compositeConverter();

    void collect();

}
