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

    /**
     * Required to set is leaf node to true for the leaf to auto-detect the context subgraph.
     * @return if is leaf node. If it is, then it will have a SubGraph created from it, with all parents automatically set.
     */
    default boolean isLeafNode() {
        return true;
    }

    default ContextValue<TestGraphContext> child() {
        return ContextValue.empty();
    }

    default ContextValue<TestGraphContext> parent() {
        return ContextValue.empty();
    }


}