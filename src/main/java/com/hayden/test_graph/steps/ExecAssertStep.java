package com.hayden.test_graph.steps;

import com.hayden.test_graph.assert_g.ctx.AssertCtx;

import java.lang.annotation.*;

/**
 * Annotates a step that finishes the initialization of the embedding into the context passed in
 * as param value, so that after the step runs the execution graph runs to perform initialization
 * logic with that initialization context.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExecAssertStep {

    /**
     * @return The context type to be initialized
     */
    Class<? extends AssertCtx>[] value();

    boolean doFnFirst() default false;

}
