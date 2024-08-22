package com.hayden.test_graph.ctx;

public sealed interface HierarchicalContext permits
        HierarchicalContext.HasParentContext,
        HierarchicalContext.HasChildContext {

    non-sealed interface HasParentContext extends HierarchicalContext, TestGraphContext {
        ContextValue<TestGraphContext> parent();

        default void doSet(HasChildContext toSet) {
            if (toSet(toSet))
                parent().set(toSet);
        }

        boolean toSet(HasChildContext toSet);
    }

    non-sealed interface HasChildContext extends HierarchicalContext, TestGraphContext{
        ContextValue<TestGraphContext> child();

        default void doSet(HasParentContext toSet) {
            if (toSet(toSet))
                child().set(toSet);
        }

        boolean toSet(HasParentContext toSet);
    }

    boolean toSet(TestGraphContext context);

    void doSet(TestGraphContext context);

}