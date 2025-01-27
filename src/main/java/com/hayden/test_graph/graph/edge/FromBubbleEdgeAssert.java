package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public interface FromBubbleEdgeAssert<T extends AssertCtx, V extends HyperGraphContext>
        extends FromBubbleEdgeT<T, AssertBubble, V> { }
