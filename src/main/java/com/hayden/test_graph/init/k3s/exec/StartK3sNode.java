package com.hayden.test_graph.init.k3s.exec;

import com.github.dockerjava.api.DockerClient;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.k3s.K3sService;
import com.hayden.test_graph.init.k3s.config.K3sInitConfigProps;
import com.hayden.test_graph.init.k3s.ctx.K3sInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.config.EnvConfigProps;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class StartK3sNode implements K3sInitNode {

    private final K3sInitConfigProps k3sInitConfigProps;

    @Override
    public boolean skip(K3sInitCtx initCtx) {
        if (k3sInitConfigProps.isSkipK3s()) {
            initCtx.getStarted().swap(true);
        }

        return k3sInitConfigProps.isSkipStartK3s();
    }

    @Override
    public K3sInitCtx exec(K3sInitCtx c, MetaCtx h) {

//        todo: use process builder to run __main__.py
        c.getStarted().swap(true);
        return c;
    }

    @Override
    public Class<K3sInitCtx> clzz() {
        return K3sInitCtx.class;
    }

}
