package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import com.hayden.test_graph.init.mountebank.exec.MbInitNode;

public interface CdMbInitNode extends MbInitNode<CdMbInitCtx> {
    @Override
    default Class<CdMbInitCtx> clzz() {
        return CdMbInitCtx.class;
    }
}
