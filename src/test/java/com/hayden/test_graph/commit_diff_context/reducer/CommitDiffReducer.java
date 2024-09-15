package com.hayden.test_graph.commit_diff_context.reducer;

import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.InitExec;
import org.springframework.stereotype.Component;

@Component
public class CommitDiffReducer implements InitExec.InitReducer{
    @Override
    public InitBubble reduce(InitCtx first, InitBubble second) {
        return second;
    }
}
