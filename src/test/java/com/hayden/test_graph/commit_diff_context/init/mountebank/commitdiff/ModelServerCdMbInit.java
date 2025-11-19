package com.hayden.test_graph.commit_diff_context.init.mountebank.commitdiff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.hayden.test_graph.commit_diff_context.init.mountebank.commitdiff.ctx.CdMbInitCtx;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.mbtest.javabank.http.core.Stub;
import org.mbtest.javabank.http.imposters.Imposter;
import org.mbtest.javabank.http.predicates.Predicate;
import org.mbtest.javabank.http.predicates.PredicateType;
import org.mbtest.javabank.http.responses.Inject;
import org.mbtest.javabank.http.responses.Is;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public interface ModelServerCdMbInit {

    Logger log = LoggerFactory.getLogger(ModelServerCdMbInit.class);

    default @NotNull Stream<Imposter> getAiServerImposter(List<CdMbInitCtx.AiServerResponseDescriptor> aiServerResponseType) {
        return fromRes(aiServerResponseType).stream();
    }

    @SneakyThrows
    default Optional<Imposter> fromRes(List<CdMbInitCtx.AiServerResponseDescriptor> aiResponses) {

        if (aiResponses.isEmpty())
            return Optional.empty();

        var nextResponse = aiResponses.getFirst();
        var next = nextResponse.requestData();

        aiResponses.sort(Comparator.comparing(CdMbInitCtx.AiServerResponseDescriptor::count));

        Stub stub = new Stub();

        for (CdMbInitCtx.AiServerResponseDescriptor nextAiResponse : aiResponses) {
            assert nextAiResponse != null;
            assert nextAiResponse.requestData().port() == next.port();
            assert Objects.equals(nextAiResponse.requestData().urlPath(), next.urlPath());

            switch(nextAiResponse.response()) {
                case CdMbInitCtx.AiServerResponse.FileSourceFunctionResponse fileSourceFunctionResponse -> {
                    Optional<String> res = fileSourceFunctionResponse.getResponseAsString();
                    if (res.isPresent())
                        addInjectResponse(res.get(), stub);
                }
                case CdMbInitCtx.AiServerResponse.FileSourceResponse fileSourceResponse ->
                        addStringResponse(nextAiResponse, stub);
                case CdMbInitCtx.AiServerResponse.StringSourceResponse stringSourceResponse ->
                        addStringResponse(nextAiResponse, stub);
            }
        }


        Predicate header = new Predicate(PredicateType.EQUALS);

        header = header.withPath(next.urlPath())
                .addHeader(getHeaderToMatch(nextResponse), "true");

        stub = stub.addPredicates(Lists.newArrayList(header));

        Imposter imposter = new Imposter();
        imposter = imposter.onPort(nextResponse.requestData().port())
                .withRequestsRecorded(true)
                .addStub(stub);

        var written = new ObjectMapper().writeValueAsString(imposter);
        log.info("{}", written);

        return Optional.of(imposter);
    }

    private static void addInjectResponse(String fn, Stub stub) {
        stub.addResponse(new Inject().withFunction(fn));
    }

    private static void addStringResponse(CdMbInitCtx.AiServerResponseDescriptor nextAiResponse, Stub stub) {
        CdMbInitCtx.ModelServerRequestData modelServerRequestData = nextAiResponse.requestData();

        Is res = new Is();

        res = res.withStatusCode(modelServerRequestData.httpStatusCode());
        res = nextAiResponse.getResponseAsString()
                .map(res::withBody)
                .or(() -> {
                    log.info("Response string was not found for mountebank.");
                    return Optional.empty();
                })
                .orElse(null);

        if (nextAiResponse.count() != -1) {
            res = res.withRepeat(nextAiResponse.repeat());
        }

        stub.addResponse(res);
    }

    static String getHeaderToMatch(CdMbInitCtx.AiServerResponseDescriptor aiServerResponse) {
        return aiServerResponse.responseType().name();
    }
}
