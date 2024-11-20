package com.hayden.test_graph.report;

import com.hayden.test_graph.meta.ctx.MetaCtx;

/**
 * Ever wanted to perform some validation to make sure that the test ran successfully?
 * So then implement this, iterate through doesMatch.prev to get your context, to make
 * sure that something happened - so your context can contain your data, and then you can
 * validate that you set some value to make sure it ran, or just perform some additional
 * assertions or validation logic over any arbitrary contextual data after it's finished.
 */
public interface ReportingValidationNode {

    boolean matches(MetaCtx doesMatch);

    void doValidateReport(MetaCtx metaCtx);

}
