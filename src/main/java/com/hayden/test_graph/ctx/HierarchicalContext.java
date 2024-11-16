package com.hayden.test_graph.ctx;

public sealed interface HierarchicalContext permits
        HierarchicalContext.HasParentContext,
        TestGraphContext {

    non-sealed interface HasParentContext extends HierarchicalContext, TestGraphContext {

        default void doSet(HasParentContext toSet) {
            if (toSet(toSet))
                parent().set(toSet);
        }

        default boolean toSet(HasParentContext toSet) {
            return false;
        }
    }

    default boolean toSet(TestGraphContext context) {
        return false;
    }

    default void doSet(TestGraphContext context) {}

    /**
     * Required to set is leaf node to true for the leaf to auto-detect the context subgraph.
     * @return if is leaf node. If it is, then it will have a SubGraph created from it, with all parents automatically set.
     */
    default boolean isLeafNode() {
        return true;
    }

    default <T extends TestGraphContext> ContextValue<T> parent() {
        return ContextValue.empty();
    }


}