package org.telegrise.telegrise.starter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.telegrise.telegrise.TelegRiseApplication;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.annotations.TreeController;

@Slf4j
public class TelegRiseBeanRegistry implements BeanDefinitionRegistryPostProcessor {
    private final TelegRiseApplication app;

    public TelegRiseBeanRegistry(TelegRiseApplication app) {
        this.app = app;
    }

    /**
     * This method adds tree controllers and update handlers as beans to the Spring registry
     */
    @Override
    public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(TreeController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Handler.class));

        for (BeanDefinition bean : scanner.findCandidateComponents(app.getMainClass().getPackageName())){
            if (bean.getBeanClassName() != null) {
                bean.setScope(BeanDefinition.SCOPE_PROTOTYPE);
                registry.registerBeanDefinition(bean.getBeanClassName(), bean);
            }
        }
    }
}
