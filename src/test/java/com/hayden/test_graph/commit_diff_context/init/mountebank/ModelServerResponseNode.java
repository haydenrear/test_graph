package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
@ResettableThread
@Profile("mb")
@Slf4j
public class ModelServerResponseNode implements CdMbInitNode, ModelServerCdMbInit {

    private static final Comparator<CdMbInitCtx.AiServerResponse.AiServerResponseType> RES = new Comparator<>() {

        List<CdMbInitCtx.AiServerResponse.AiServerResponseType> SERVER = List.of(
                CdMbInitCtx.AiServerResponse.AiServerResponseType.EMBEDDING,
                CdMbInitCtx.AiServerResponse.AiServerResponseType.INITIAL_CODE,
                CdMbInitCtx.AiServerResponse.AiServerResponseType.CODEGEN
        );

        @Override
        public int compare(CdMbInitCtx.AiServerResponse.AiServerResponseType o1,
                           CdMbInitCtx.AiServerResponse.AiServerResponseType o2) {
            return Integer.compare(SERVER.indexOf(o1), SERVER.indexOf(o2));
        }
    };

    @SneakyThrows
    @Override
    public Stream<Imposter> createGetImposters(CdMbInitCtx ctx) {
        return Arrays.stream(CdMbInitCtx.AiServerResponse.AiServerResponseType.values())
                .sorted(RES)
                .flatMap(n -> getAiServerImposter(ctx, n));
    }

}
