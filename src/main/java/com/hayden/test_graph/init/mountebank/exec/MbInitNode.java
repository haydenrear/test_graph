package com.hayden.test_graph.init.mountebank.exec;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.InitExec;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.json.simple.parser.ParseException;
import org.mbtest.javabank.http.imposters.Imposter;

import java.util.Optional;
import java.util.stream.Stream;

public interface MbInitNode<T extends MbInitCtx> extends InitNode<T> {

    Stream<Imposter> createGetImposters(T ctx);

    @Override
    default T exec(T c, MetaCtx h) {
        createGetImposters(c)
                .forEach(imposterCreated -> {
                    createGetImposter(c, imposterCreated);
                });
        return c;
    }

    default void createGetImposter(T c, Imposter imposterCreated) {
        try {
            Imposter imposter = c.client().getImposter(imposterCreated.getPort());
            Optional.ofNullable(imposter)
                    .ifPresentOrElse(i -> {
                                imposterCreated.getStubs().forEach(i::addStub);
                                createDeleteImposter(c, imposterCreated);
                            },
                            () -> c.client().createImposter(imposterCreated));
        } catch (ParseException e) {
            log.error("Error creating imposter, attempting to add new imposter.");
            createDeleteImposter(c, imposterCreated);
        }
    }

    private void createDeleteImposter(T c, Imposter imposterCreated) {
        log.info("{}", c.client().deleteImposter(imposterCreated.getPort()));
        log.info("{}", c.client().createImposter(imposterCreated));
    }

    @Override
    default T exec(T c, HyperGraphContext hgCtx, MetaCtx h) {
        return exec(c, h);
    }

}
