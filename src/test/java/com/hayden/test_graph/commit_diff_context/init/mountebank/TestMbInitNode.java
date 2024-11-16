package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.mbtest.javabank.http.core.Stub;
import org.mbtest.javabank.http.imposters.Imposter;
import org.mbtest.javabank.http.predicates.Predicate;
import org.mbtest.javabank.http.predicates.PredicateType;
import org.mbtest.javabank.http.responses.Is;
import org.mbtest.javabank.http.responses.Response;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@ResettableThread
@Profile("mb")
public class TestMbInitNode implements CdMbInitNode {
    @Override
    public Stream<Imposter> createGetImposters(CdMbInitCtx ctx) {
        Stub stub = new Stub();
        Predicate e1 = new Predicate(PredicateType.EQUALS);
        e1.withPath("/hello");
        stub.addPredicates(List.of(e1));
        Response response = new Is();
        response.put("hello", "goodbye");
        stub.addResponse(response);
        return Stream.of(Imposter.anImposter().onPort(2525).withStub(stub));
    }

    @Override
    public Class<CdMbInitCtx> clzz() {
        return CdMbInitCtx.class;
    }

}
