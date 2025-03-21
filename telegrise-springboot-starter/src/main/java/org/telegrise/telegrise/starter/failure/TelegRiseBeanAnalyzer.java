package org.telegrise.telegrise.starter.failure;

import org.springframework.beans.factory.BeanCreationException;
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
            if (rootFailure instanceof BeanCreationException creationException)
                return new FailureAnalysis(
                    "The bean %s can be used only in tree controllers and non-independent handlers; '%s' is not one".formatted(
                            cause.getBeanType().getSimpleName(), creationException.getBeanName()),
                    null, cause);
            else
                return new FailureAnalysis(
                        "The bean %s can be used only in tree controllers and non-independent handlers".formatted(cause.getBeanType().getSimpleName()),
                        null, cause);
        }

        return null;
    }
}
