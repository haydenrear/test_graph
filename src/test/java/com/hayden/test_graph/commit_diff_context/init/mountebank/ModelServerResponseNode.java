package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.proto.prototyped.datasources.ai.modelserver.client.ModelContextProtocolClientAdapter;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.ai.mcp.client.transport.ServerParameters;
import org.springframework.ai.mcp.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.spec.ClientMcpTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@ResettableThread
@Profile("mb")
@Slf4j
public class ModelServerResponseNode implements CdMbInitNode, ModelServerCdMbInit {

    private static final Comparator<CdMbInitCtx.AiServerResponse.AiServerResponseType> RES = new Comparator<>() {

        List<CdMbInitCtx.AiServerResponse.AiServerResponseType> SERVER = List.of(
                CdMbInitCtx.AiServerResponse.AiServerResponseType.TOOLSET
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
//        var s = new ModelContextProtocolClientAdapter();
//        var m = Map.of("name", "query", "arguments", Map.of("sql", "SELECT * FROM commit_diff"));
//        var r = new McpSchema.JSONRPCRequest("2.0", "tools/call", "whatever!", m);
//        var called = s.doCallClient(
//                new ModelContextProtocolClientAdapter.CallClientParams(ServerParameters.builder("docker")
//                        .args("run", "-i", "--rm", "mcp/postgres", "postgresql://postgres:postgres@host.docker.internal:5450/postgres")
//                        .build(),
//                        r)
//                )
//                .block();
//        Thread.sleep(20000);
        return Arrays.stream(CdMbInitCtx.AiServerResponse.AiServerResponseType.values())
                .sorted(RES)
                .flatMap(n -> getAiServerImposter(ctx, n));
    }

}
