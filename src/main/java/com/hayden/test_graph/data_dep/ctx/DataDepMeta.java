package com.hayden.test_graph.data_dep.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
@Scope("prototype")
public class DataDepMeta implements MetaCtx {
    @Delegate
    @Getter
    DataDepBubble bubbled;

    final Stack<HyperGraphContext<MetaCtx>> prev = new Stack<>();

    public DataDepMeta() {
    }

    public boolean isLeafNode() {return false;}

    public void setBubbled(DataDepBubble bubble) {
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
