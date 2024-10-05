package com.hayden.test_graph.assert_g.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Stack;

@Component
@Scope("prototype")
public class AssertMeta implements MetaCtx {

    @Delegate
    @Getter
    AssertBubble bubbled;

    final Stack<HyperGraphContext<MetaCtx>> prev = new Stack<>();

    public boolean isLeafNode() {
        return false;
    }

    public AssertMeta() {
    }

    public void setBubbled(AssertBubble bubbled) {
        this.bubbled = bubbled;
        prev.push(bubbled);
    }

    @Override
    public boolean executableFor(MetaProgNode n) {
        return true;
    }

    public Stack<? extends HyperGraphContext> prev() {
        return prev;
    }



}
