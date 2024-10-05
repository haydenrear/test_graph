package com.hayden.test_graph.init.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
@Scope("prototype")
public class InitMeta implements MetaCtx {

    @Delegate
    @Getter
    InitBubble bubbled;

    public boolean isLeafNode() {return false;}

    final Stack<HyperGraphContext<MetaCtx>> prev = new Stack<>();

    public void setBubbled(InitBubble bubble) {
        this.bubbled = bubble;
        prev.push(bubble);
    }

    @Override
    public boolean executableFor(MetaProgNode n) {
        return true;
    }

    public Stack<? extends HyperGraphContext> prev() {
        return prev;
    }

}
