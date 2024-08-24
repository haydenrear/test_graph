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

    long timeoutMillis() default 0L;

}
