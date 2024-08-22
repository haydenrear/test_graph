package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgCtxConverterComposite implements ProgCtxConvert<HyperGraphContext> {

    List<ProgCtxConvert> converters;

    @Autowired
    @Lazy
    public void setConverters(List<ProgCtxConvert> converters) {
        this.converters = converters;
    }

    @Override
    public MetaCtx visit(HyperGraphContext testGraphNodeContextComposite,
                         MetaCtx ctx) {
        for (var converter : converters) {
            ctx = converter.visit(testGraphNodeContextComposite, ctx);
        }

        return ctx;
    }
}
