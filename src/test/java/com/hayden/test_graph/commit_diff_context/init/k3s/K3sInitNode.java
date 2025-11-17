package com.hayden.test_graph.commit_diff_context.init.k3s;

import com.hayden.test_graph.commit_diff_context.init.k3s.ctx.K3sInit;
import com.hayden.test_graph.init.exec.single.InitNode;

public interface K3sInitNode extends InitNode<K3sInit> {

    default Class<? extends K3sInit> clzz() {
        return K3sInit.class;
    }
}
