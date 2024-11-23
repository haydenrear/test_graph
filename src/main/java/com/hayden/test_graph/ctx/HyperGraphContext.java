package com.hayden.test_graph.ctx;

import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface HyperGraphContext<SELF extends HyperGraphContext<SELF>> extends TestGraphContext<SELF>{

    @Override
    default Class<? extends SELF> bubbleClazz() {
        return (Class<? extends SELF>) bubble().getClass();
    }

    @Override
    default SELF bubble() {
        return (SELF) this;
    }

    default MetaCtx bubbleMeta(MetaCtx metaCtx) {
        return metaCtx;
    }

    /**
     * @return list of classes that bubble this context.
     */
    List<Class<? extends TestGraphContext>> bubblers();

    /**
     * TODO:
     * @param other
     * @return
     */
    default SELF merge(SELF other) {
        return other;
    }

}
