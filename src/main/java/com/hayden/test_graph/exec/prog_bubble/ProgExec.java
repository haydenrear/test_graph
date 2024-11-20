package com.hayden.test_graph.exec.prog_bubble;

import com.hayden.test_graph.ctx.GraphContext;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.HyperGraph;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface ProgExec  {

    Logger log = LoggerFactory.getLogger(ProgExec.class);

    interface ProgExecNode<CTX extends HyperGraphContext<MetaCtx>> extends GraphExec<TestGraphContext<CTX>, CTX> {

        default CTX preMap(CTX c, MetaCtx h) {
            return c;
        }

        default CTX postMap(CTX c, MetaCtx h) {
            return c;
        }

        default CTX exec(CTX c, MetaCtx h) {
            return c;
        }

        default CTX exec(CTX c, MetaCtx hgCtx, MetaCtx h) {
            return exec(c, h);
        }

    }

    interface ProgExecReducer extends GraphExec.GraphExecReducer<MetaCtx, MetaCtx> {
    }

    MetaCtx collectCtx();

    default MetaCtx exec(Class<? extends TestGraphContext> ctx) {
        return this.exec(ctx, null) ;
    }

    MetaCtx exec(Class<? extends TestGraphContext> ctx, MetaCtx prev);

    void register(Class<? extends TestGraphContext> ctx);

    void execAll();

}
