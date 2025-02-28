package org.telegrise.telegrise.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.telegrise.telegrise.MediaCollector;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.TranscriptionManager;
import org.telegrise.telegrise.core.ResourceInjector;
import org.telegrise.telegrise.senders.BotSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component @RequiredArgsConstructor @Slf4j
public class TelegRiseSessionContextProvider {
    private static final Class<?>[] SESSION_RESOURCES = new Class[]
            {BotSender.class, SessionMemory.class, TranscriptionManager.class, MediaCollector.class};

    private final Map<SessionIdentifier, ApplicationContext> contextMap = new ConcurrentHashMap<>();
    private final ApplicationContext mainContext;

    public Object getBean(Class<?> beanType, ResourceInjector injector){
        var applicationContext = get(injector);

        try {
            return applicationContext.getBean(beanType.getName());
        } catch (NoSuchBeanDefinitionException e){
            if (!(applicationContext instanceof GenericApplicationContext context))  //FIXME
                throw e;

            var definition = new GenericBeanDefinition();
            definition.setBeanClass(beanType);
            definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            context.registerBeanDefinition(beanType.getName(), definition);

            return context.getBean(beanType.getName());
        }
    }

    public @NotNull ApplicationContext get(ResourceInjector injector){
        var memory = injector.get(SessionMemory.class, null);
        if (memory == null) return mainContext;

        return contextMap.computeIfAbsent(memory.getSessionIdentifier(), id -> createSessionContext(id, injector));
    }

    private @NotNull ApplicationContext createSessionContext(SessionIdentifier identifier, ResourceInjector injector) {
        var context = new AnnotationConfigApplicationContext();
        context.setParent(mainContext);

        for (Class<?> c : SESSION_RESOURCES) {
            Object instance = injector.get(c, null);
            if (instance == null)
                log.error("Found an empty bean of type {} for session {}", c, identifier);
            else {
                var definition = new GenericBeanDefinition();
                definition.setBeanClass(c);
                definition.setScope(BeanDefinition.SCOPE_SINGLETON);
                definition.setPrimary(true);
                definition.setInstanceSupplier(() -> instance);

                context.registerBeanDefinition(c.getName(), definition);
            }
        }

        context.refresh();
        return context;
    }
}
