package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class TestInitCtx implements InitCtx {
    @Override
    public InitBubble bubble() {
        return new InitBubble() {
            @Override
            public MetaCtx bubble() {
                return new InitMeta(this);
            }

            @Override
            public boolean executableFor(GraphNode n) {
                return true;
            }

            @Override
            public boolean toSet(TestGraphContext context) {
                return false;
            }

            @Override
            public void doSet(TestGraphContext context) {

            }

            @Override
            public boolean isExecutable() {
                return false;
            }

            @Override
            public ContextValue<TestGraphContext> child() {
                return ContextValue.empty();
            }

            @Override
            public ContextValue<TestGraphContext> parent() {
                return ContextValue.empty();
            }

            @Override
            public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
                return List.of();
            }
        };
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof InitNode<?>;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {
    }

    @Override
    public boolean isExecutable() {
        return true;
    }

    @Override
    public ContextValue<TestGraphContext> child() {
        return ContextValue.empty();
    }

    @Override
    public ContextValue<TestGraphContext> parent() {
        return ContextValue.empty();
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        return List.of();
    }
}
