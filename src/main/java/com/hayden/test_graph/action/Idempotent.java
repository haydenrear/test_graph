package com.hayden.test_graph.action;


import java.lang.annotation.*;

/**
 * If a method gets called once it will cache the result - or the fact that it called it,
 * and not call again / return the cached result.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * @return the arg index to return if not calling - ret type must be same as that arg index.
     */
    int returnArg() default -1;

    long timeoutMillis() default 0L;

    Class<? extends DoRunAgain> runAgain() default DoRunAgain.class;

}
