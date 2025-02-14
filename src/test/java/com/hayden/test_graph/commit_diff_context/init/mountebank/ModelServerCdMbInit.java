package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.mbtest.javabank.http.core.Stub;
import org.mbtest.javabank.http.imposters.Imposter;
import org.mbtest.javabank.http.predicates.Predicate;
import org.mbtest.javabank.http.predicates.PredicateType;
import org.mbtest.javabank.http.responses.Is;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

public interface ModelServerCdMbInit {

    Logger log = LoggerFactory.getLogger(ModelServerCdMbInit.class);

    default @NotNull Stream<Imposter> getAiServerImposter(CdMbInitCtx ctx,
                                                          CdMbInitCtx.AiServerResponse.AiServerResponseType aiServerResponseType) {
        var responses = ctx.getServerResponses();
        return responses.responses().stream()
                .filter(ai -> ai.responseType() == aiServerResponseType)
                .flatMap(a -> fromRes(a).stream());
    }

    default Optional<Imposter> fromRes(CdMbInitCtx.AiServerResponseDescriptor response) {

        Stub stub = new Stub();

        CdMbInitCtx.ModelServerRequestData modelServerRequestData = response.requestData();

        Is res = new Is();

        res = res.withStatusCode(modelServerRequestData.httpStatusCode());
        res = response.getResponseAsString()
                .map(res::withBody)
                .or(() -> {log.info("Response string was not found for mountebank."); return Optional.empty();})
                .orElse(null);

        if (response.count() != -1)
            res = res.withCount(response.count());

        stub = stub.addResponse(res);

        Predicate header = new Predicate(PredicateType.EQUALS);

        header = header.withPath(modelServerRequestData.urlPath())
                .addHeader(getHeaderToMatch(response), "true");

        stub = stub.addPredicates(Lists.newArrayList(header));

        Imposter imposter = new Imposter();
        imposter = imposter.onPort(response.requestData().port())
                .addStub(stub);

        return Optional.of(imposter);
    }

    static String getHeaderToMatch(CdMbInitCtx.AiServerResponseDescriptor aiServerResponse) {
        return switch(aiServerResponse.responseType()) {
            case EMBEDDING -> "EMBEDDING";
            case CODEGEN -> "CODEGEN";
            case TOOLSET -> "TOOLSET";
            case INITIAL_CODE -> "INITIAL_CODE";
            case VALIDATION -> "VALIDATION";
        };
    }
}
