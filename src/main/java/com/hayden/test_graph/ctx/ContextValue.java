package com.hayden.test_graph.ctx;

import com.hayden.utilitymodule.result.MutableResult;
import com.hayden.utilitymodule.result.Result;
import lombok.experimental.Delegate;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record ContextValue<T>(MutableContextValue<T, ContextValueError> res) {

    public void clear() {
        res.res = Result.mutableEmpty();
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

    public synchronized void swap(T t) {
        this.res.set(t);
    }

    public synchronized void compareAndSwap(Predicate<T> curr, T t) {
        if (curr.test(this.optional().orElse(null))) {
            this.res.set(t);
        }
    }

    public static class MutableContextValue<T, E> {
        @Delegate
        MutableResult<T, E> res;

        Stack<MutableResult<T, E>> old = new Stack<>();

        public MutableContextValue(MutableResult<T, E> res) {
            this.res = res;
        }

        public MutableContextValue(T t) {
            this.res = Result.mutableOk(t);
        }

        public static <T, E> MutableContextValue<T, E> empty() {
            return new MutableContextValue<>(Result.mutableEmpty());
        }

        public static <T> MutableContextValue<T, ContextValueError> ofExisting(T t) {
            return new MutableContextValue<>(t);
        }

        public void set(T t) {
            this.res.set(t);
        }

        public void replace(MutableResult<T, E> res) {
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
