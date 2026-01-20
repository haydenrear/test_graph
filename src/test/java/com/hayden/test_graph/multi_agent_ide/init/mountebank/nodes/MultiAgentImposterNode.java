package com.hayden.test_graph.multi_agent_ide.init.mountebank.nodes;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx.MultiAgentIdeMbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.stream.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                        Path resolved = resolvePath(e.getValue());
                        if (resolved == null) {
                            assertions.assertSoftly(false, "Imposter file %s could not be resolved".formatted(e.getValue()));
                            return Stream.empty();
                        }
                        JSONObject json = (JSONObject) new JSONParser().parse(Files.newBufferedReader(resolved));
                        JSONArray imposters = (JSONArray) (json.get("impostors") != null
                                ? json.get("impostors")
                                : json.get("imposters"));
                        if (imposters == null) {
                            assertions.assertSoftly(false, "No impostors found in %s".formatted(resolved));
                            return Stream.empty();
                        }
                        return imposters.stream()
                                .filter(JSONObject.class::isInstance)
                                .map(entry -> Imposter.fromJSON((JSONObject) entry));
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

    private Path resolvePath(Path provided) {
        if (provided == null) {
            return null;
        }
        if (Files.exists(provided)) {
            return provided;
        }
        Path repoRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path base = repoRoot;
        if ("test_graph".equals(repoRoot.getFileName() != null ? repoRoot.getFileName().toString() : "")) {
            base = repoRoot.resolve("src").resolve("test").resolve("resources");
        } else {
            base = repoRoot.resolve("test_graph").resolve("src").resolve("test").resolve("resources");
        }
        Path candidate = base.resolve(provided);
        if (Files.exists(candidate)) {
            return candidate;
        }
        try {
            var resource = Thread.currentThread().getContextClassLoader().getResource(provided.toString());
            if (resource != null) {
                return Paths.get(resource.toURI());
            }
        } catch (Exception ignored) {
            // ignore resource resolution failures
        }
        return null;
    }

}
