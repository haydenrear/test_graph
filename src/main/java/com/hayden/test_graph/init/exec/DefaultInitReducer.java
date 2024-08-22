package com.hayden.test_graph.init.exec;

import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import org.springframework.stereotype.Component;

@Component
public class DefaultInitReducer implements InitExec.InitReducer {
    @Override
    public InitBubble reduce(InitCtx first, InitBubble second) {
        return first.bubble();
    }
}
