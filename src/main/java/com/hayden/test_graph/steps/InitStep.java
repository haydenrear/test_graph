package com.hayden.test_graph.steps;

import com.hayden.test_graph.init.ctx.InitCtx;
import org.springframework.context.annotation.Scope;

import java.lang.annotation.*;

/**
 * Annotates a step that finishes the initialization of the data into the context passed in
 * as param value.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InitStep {

    /**
     * @return The context type to be initialized
     */
    Class<? extends InitCtx> value();

}
