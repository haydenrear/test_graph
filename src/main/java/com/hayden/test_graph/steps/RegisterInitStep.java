package com.hayden.test_graph.steps;

import com.hayden.test_graph.init.ctx.InitCtx;

import java.lang.annotation.*;

/**
 * Annotates a step that finishes the initialization of the embedding into the context passed in
 * as param value, so that after the step runs the execution graph runs to perform initialization
 * logic with that initialization context.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterInitStep {

    /**
     * @return The context type to be initialized
     */
    Class<? extends InitCtx>[] value();

}
