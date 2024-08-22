package com.hayden.test_graph.ctx;

public sealed interface HierarchicalContext permits
        HierarchicalContext.HasParentContext,
        HierarchicalContext.HasChildContext,
        TestGraphContext {

    non-sealed interface HasParentContext extends HierarchicalContext, TestGraphContext {

        default void doSet(HasChildContext toSet) {
            if (toSet(toSet))
                parent().set(toSet);
        }

        boolean toSet(HasChildContext toSet);
    }

    non-sealed interface HasChildContext extends HierarchicalContext, TestGraphContext{

        default void doSet(HasParentContext toSet) {
            if (toSet(toSet))
                child().set(toSet);
        }

        boolean toSet(HasParentContext toSet);
    }

    boolean toSet(TestGraphContext context);

    void doSet(TestGraphContext context);

    boolean isExecutable();

    ContextValue<TestGraphContext> child();

    ContextValue<TestGraphContext> parent();


}