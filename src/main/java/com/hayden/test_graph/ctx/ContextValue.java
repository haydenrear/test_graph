package com.hayden.test_graph.ctx;

import com.hayden.utilitymodule.result.Result;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public record ContextValue<T>(@Delegate MutableContextValue<T, ContextValueError> res) {

    public record ContextValueError() {}

    public void set(T t) {
        this.res.set(t);
    }

    public static class MutableContextValue<T, E> {
        @Delegate
        Result<T, E> res;

        Stack<Result<T, E>> old = new Stack<>();

        public void set(T t) {
            this.res.r().set(t);
        }

        public void replace(Result<T, E> res) {
            this.old.push(this.res);
            this.res = res;
        }

        public Result<T, E> pop() {
            this.res = old.pop();
            return this.res;
        }

    }

}
