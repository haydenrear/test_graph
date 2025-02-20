package com.hayden.test_graph.commit_diff_context.init.mountebank.ctx;

import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.io.FileUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@ResettableThread
@RequiredArgsConstructor
public class CdMbInitCtx implements MbInitCtx {

    public record ModelServerRequestData(String urlPath, int httpStatusCode, int port) {}

    public record AiServerResponseDescriptor(int count, int repeat, @Delegate AiServerResponse response) {
        public AiServerResponseDescriptor(AiServerResponse response) {
            this(-1, 1, response);
        }
    }

    public interface AiServerResponse {

        enum AiServerResponseType {
            EMBEDDING, CODEGEN, TOOLSET, INITIAL_CODE, VALIDATION
        }

        record FileSourceResponse(Path filePath, AiServerResponseType responseType,
                                  ModelServerRequestData requestData) implements AiServerResponse {
            @Override
            public Optional<String> getResponseAsString() {
                return FileUtils.readToString(filePath.toFile())
                        .one()
                        .mapError(se -> {
                            log.error("Error when reading {}.", filePath.toFile());
                            return se;
                        })
                        .optional();
            }
        }

        record StringSourceResponse(String res, AiServerResponseType responseType,
                                    ModelServerRequestData requestData) implements AiServerResponse {
            @Override
            public Optional<String> getResponseAsString() {
                return Optional.ofNullable(res);
            }
        }

        Optional<String> getResponseAsString();

        AiServerResponseType responseType();

        ModelServerRequestData requestData();

    }


    public record AiServerResponses(List<AiServerResponseDescriptor> responses) {}

    private Client client;

    private final ContextValue<CdMbInitBubbleCtx> bubbleUnderlying;

    private final ContextValue<AiServerResponses> serverResponses;

    public CdMbInitCtx() {
        this(ContextValue.empty(), ContextValue.empty());
    }

    public AiServerResponses getServerResponses() {
        return serverResponses.res().orElseGet(() -> {
            log.warn("No server responses available - requesting non-existent server responses. Sort of weird.");
            return new AiServerResponses(new ArrayList<>());
        });
    }

    public void setAiResponses(AiServerResponses serverResponses) {
        this.serverResponses.swap(serverResponses);
    }

    public void addAiServerResponse(AiServerResponse serverResponse) {
        addAiServerResponse(serverResponse, -1);
    }

    public void addAiServerResponse(AiServerResponse serverResponse, int count, int numRepetitions) {
        AiServerResponses t;
        if (serverResponses.isEmpty()) {
            t = new AiServerResponses(new ArrayList<>());
            serverResponses.swap(t);
        } else {
            t = serverResponses.res().get();
        }

        t.responses.add(new AiServerResponseDescriptor(count, numRepetitions, serverResponse));
    }

    public void addAiServerResponse(AiServerResponse serverResponse, int count) {
        this.addAiServerResponse(serverResponse, count, 1);
    }

    @Autowired
    public void setBubble(CdMbInitBubbleCtx bubble) {
        this.bubbleUnderlying.swap(bubble);
    }

    @Override
    public Client client() {
        return this.client;
    }

    @Override
    @Autowired
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public CdMbInitBubbleCtx bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<CdMbInitBubbleCtx> bubbleClazz() {
        return CdMbInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitNode;
    }
}
