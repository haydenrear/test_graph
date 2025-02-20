package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ResettableThread
@Profile("mb")
@Slf4j
public class ModelServerResponseNode implements CdMbInitNode, ModelServerCdMbInit {

    private static final Comparator<CdMbInitCtx.AiServerResponse.AiServerResponseType> RES = new Comparator<>() {

        final List<CdMbInitCtx.AiServerResponse.AiServerResponseType> SERVER = List.of(
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
        var g = ctx.getServerResponses().responses().stream()
                .collect(Collectors.groupingBy(CdMbInitCtx.AiServerResponseDescriptor::responseType));

        return g.keySet().stream().sorted(RES)
                .map(r -> Map.entry(r, g.get(r)))
                .flatMap(entry -> getAiServerImposter(entry.getValue()));

    }

}
