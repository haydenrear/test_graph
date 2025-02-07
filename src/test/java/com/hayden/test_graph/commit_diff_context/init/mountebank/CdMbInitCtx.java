package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.io.FileUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: add edge and embedding so it sets embedding to call imposters
@Slf4j
@Component
@ResettableThread
@RequiredArgsConstructor
public class CdMbInitCtx implements MbInitCtx {

    public record ModelServerRequestData(String urlPath, int httpStatusCode, int port) {}


    public interface AiServerResponse {

        enum AiServerResponseType {
            EMBEDDING, CODEGEN, TOOLSET, INITIAL_CODE
        }

        record FileSourceResponse(Path filePath, AiServerResponseType responseType,
                                  ModelServerRequestData requestData) implements AiServerResponse {
            @Override
            public Optional<String> getResponseAsString() {
                return FileUtils.readToString(filePath.toFile())
                        .one()
                        .mapError(se -> {
                            log.error("Error when reading %s.".formatted(filePath.toFile()));
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


    public record AiServerResponses(List<AiServerResponse> responses) {}

    Client client;

    private final ContextValue<CdMbInitBubbleCtx> bubbleUnderlying;

    @Getter
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
        AiServerResponses t;
        if (serverResponses.isEmpty()) {
            t = new AiServerResponses(new ArrayList<>());
            serverResponses.swap(t);
        } else {
            t = serverResponses.res().get();
        }

        t.responses.add(serverResponse);
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
