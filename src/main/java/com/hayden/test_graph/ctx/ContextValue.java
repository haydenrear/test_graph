package com.hayden.test_graph.ctx;

import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.ok.Ok;
import lombok.experimental.Delegate;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;

public record ContextValue<T>(MutableContextValue<T, ContextValueError> res) {

    public void clear() {
        res.res = Result.empty();
    }

    public boolean isEmpty() {
        return !res.res.one().isPresent();
    }

    public Optional<T> optional() {
        return res.one().optional();
    }

    public boolean isPresent() {
        return res.res.one().isPresent();
    }

    public static <T> ContextValue<T> empty() {
        return new ContextValue<>(MutableContextValue.empty());
    }

    public static <T> ContextValue<T> ofExisting(T t) {
        return new ContextValue<>(MutableContextValue.ofExisting(t));
    }

    public record ContextValueError() {}

    public void set(T t) {
        this.res.set(t);
    }

    public static class MutableContextValue<T, E> {
        @Delegate
        Result<T, E> res;

        Stack<Result<T, E>> old = new Stack<>();

        public MutableContextValue(Result<T, E> res) {
            this.res = res;
        }

        public MutableContextValue(T t) {
            this.res = Result.ok(Ok.ok(t));
        }

        public static <T, E> MutableContextValue<T, E> empty() {
            return new MutableContextValue<>(Result.empty());
        }

        public static <T> MutableContextValue<T, ContextValueError> ofExisting(T t) {
            return new MutableContextValue<>(t);
        }

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

        public T orElseThrow(Supplier<RuntimeException> throwable) {
            if (!this.res.one().isPresent()) {
                throw throwable.get();
            }
            return this.res.one().get();
        }

        public T orElseThrow() {
            return orElseThrow(() -> new RuntimeException("Failed to retrieve value."));
        }
    }

}
