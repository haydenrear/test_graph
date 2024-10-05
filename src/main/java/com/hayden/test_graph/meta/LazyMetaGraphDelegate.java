package com.hayden.test_graph.meta;

import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LazyMetaGraphDelegate {

    @Lazy
    @ResettableThread
    @Autowired
    @Delegate
    MetaGraphDelegate autoDetect;
}
