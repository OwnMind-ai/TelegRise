package org.telegrise.telegrise.starter.failure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

public class TelegRiseRuntimeExceptionAnalyzer extends AbstractFailureAnalyzer<TelegRiseRuntimeException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, TelegRiseRuntimeException cause) {
        return new FailureAnalysis(cause.toString(), null, cause);
    }
}
