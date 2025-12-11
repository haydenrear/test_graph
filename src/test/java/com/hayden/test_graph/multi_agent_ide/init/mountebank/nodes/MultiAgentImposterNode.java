package com.hayden.test_graph.multi_agent_ide.init.mountebank.nodes;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx.MultiAgentIdeMbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.stream.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;


@Component
@ResettableThread
@Profile("mb")
@Slf4j
public class MultiAgentImposterNode implements MultiAgentIdeMbInitNode {

    @Autowired
    @ResettableThread
    Assertions assertions;

    @Override
    public Stream<Imposter> createGetImposters(MultiAgentIdeMbInitCtx ctx) {
        return StreamUtil.toStream(ctx.getMockResponses().impostorsByName())
                .flatMap(e -> StreamUtil.toStream(e.entrySet()))
                .flatMap(e -> {
                    try {
                        return Stream.of(Imposter.fromJSON(null));
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        assertions.assertSoftly(false, "Was not able to read %s, %s, %s".formatted(e.getKey(), e.getValue(), ex));
                        return Stream.empty();
                    }
                });
    }

    @Override
    public Class<? extends MultiAgentIdeMbInitCtx> clzz() {
        return MultiAgentIdeMbInitCtx.class;
    }

}
