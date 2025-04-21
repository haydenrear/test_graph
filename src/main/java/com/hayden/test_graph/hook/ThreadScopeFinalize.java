package com.hayden.test_graph.hook;

import com.hayden.test_graph.thread.ResettableThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThreadScopeFinalize implements FinalizeHook {

    @Autowired
    ResettableThreadScope resettableThreadScope;

    @Override
    public Void call() {
        resettableThreadScope.reset();
        return null;
    }
}
