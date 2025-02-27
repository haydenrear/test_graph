package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.google.common.collect.Lists;
import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import org.jetbrains.annotations.NotNull;
import org.mbtest.javabank.http.core.Stub;
import org.mbtest.javabank.http.imposters.Imposter;
import org.mbtest.javabank.http.predicates.Predicate;
import org.mbtest.javabank.http.predicates.PredicateType;
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


        Predicate header = new Predicate(PredicateType.EQUALS);

        header = header.withPath(next.urlPath())
                .addHeader(getHeaderToMatch(nextResponse), "true");

        stub = stub.addPredicates(Lists.newArrayList(header));

        Imposter imposter = new Imposter();
        imposter = imposter.onPort(nextResponse.requestData().port())
                .withRequestsRecorded(true)
                .addStub(stub);

        return Optional.of(imposter);
    }

    static String getHeaderToMatch(CdMbInitCtx.AiServerResponseDescriptor aiServerResponse) {
        return aiServerResponse.responseType().name();
    }
}
