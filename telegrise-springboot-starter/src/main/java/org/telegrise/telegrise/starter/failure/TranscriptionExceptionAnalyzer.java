package org.telegrise.telegrise.starter.failure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

public class TranscriptionExceptionAnalyzer extends AbstractFailureAnalyzer<TranscriptionParsingException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, TranscriptionParsingException cause) {
        return new FailureAnalysis(cause.toString(), null, cause);
    }
}
