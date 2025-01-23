package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Stream;

@Component
@ResettableThread
@Profile("mb")
@Slf4j
public class ModelServerResponseNode implements CdMbInitNode, ModelServerCdMbInit {

    @Override
    public Stream<Imposter> createGetImposters(CdMbInitCtx ctx) {
        return Arrays.stream(CdMbInitCtx.AiServerResponse.AiServerResponseType.values())
                .flatMap(n -> getAiServerImposter(ctx, n));
    }

}
