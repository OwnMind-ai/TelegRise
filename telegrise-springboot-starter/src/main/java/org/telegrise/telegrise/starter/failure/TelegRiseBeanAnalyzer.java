package org.telegrise.telegrise.starter.failure;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.telegrise.telegrise.MediaCollector;
import org.telegrise.telegrise.SessionMemory;

import java.util.List;

public class TelegRiseBeanAnalyzer extends AbstractFailureAnalyzer<NoSuchBeanDefinitionException> {
    private static final List<Class<?>> CLASSES = List.of(SessionMemory.class, MediaCollector.class);

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, NoSuchBeanDefinitionException cause) {
        if (cause.getBeanType() != null && CLASSES.contains(cause.getBeanType())) {
            return new FailureAnalysis(
                    "The bean %s can be used only in tree controllers and non-independent handlers".formatted(cause.getBeanType().getSimpleName()),
                    null, cause);
        }

        return null;
    }
}
