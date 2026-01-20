package com.hayden.test_graph.init.selenium.exec;

import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.init.selenium.ctx.SeleniumInitBubbleCtx;

public interface SeleniumInitBubbleNode extends InitBubbleNode<SeleniumInitBubbleCtx> {

    default Class<SeleniumInitBubbleCtx> clzz() {
        return SeleniumInitBubbleCtx.class;
    }
}
