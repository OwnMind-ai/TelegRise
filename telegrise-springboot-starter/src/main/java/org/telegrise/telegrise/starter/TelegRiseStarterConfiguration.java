package org.telegrise.telegrise.starter;

import lombok.Setter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.*;
import org.telegrise.telegrise.core.ResourceInjector;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.senders.BotSender;

import java.io.File;

@Setter
@Configuration
@ComponentScan
public class TelegRiseStarterConfiguration {
    @Bean
    public TelegRiseApplication telegRiseApplication(ConfigurableApplicationContext context) {
        var transcription = context.getEnvironment().getProperty("telegrise.transcription");
        if (transcription == null || !(new File(transcription).exists()))
            throw new TelegRiseRuntimeException("No transcription file specified or file doesn't exists. Please, add 'telegrise.transcription'" +
                    " to the application properties with a valid path to the transcription file.");
        return new TelegRiseApplication(new File(transcription), this.getApplicationClass(context));
    }

    @Bean
    public ApplicationRunner telegRiseRunner(ConfigurableApplicationContext context, TelegRiseApplication app){
        return args -> {
            context.getBeansOfType(Service.class).values().forEach(app::addService);
            context.getBeansOfType(SessionInitializer.class).values().stream().findFirst().ifPresent(app::setSessionInitializer);
            context.getBeansOfType(RoleProvider.class).values().stream().findFirst().ifPresent(app::setRoleProvider);
            context.getBeansOfType(TelegRiseExecutorService.class).values().stream().findFirst()
                    .ifPresent(e -> app.setExecutorService(() -> e));

            ResourceInjector.setInstanceInitializer(context::getBean);

            app.start();
        };
    }

    private Class<?> getApplicationClass(ApplicationContext context) {
        return context.getBeansWithAnnotation(SpringBootApplication.class).values().stream()
                .map(o -> (Class<?>) o.getClass()).findFirst()
                .orElseThrow(() -> new TelegRiseRuntimeException("No @SpringBootApplication class was found"));
    }

    @Bean
    public TelegRiseBeanRegistry beanRegistry(TelegRiseApplication app){
        return new TelegRiseBeanRegistry(app);
    }

    @Bean
    public TelegramClient telegramClient(TelegRiseApplication app){
        return app.getResourceProvider().get(TelegramClient.class, null);
    }

    @Bean
    @Scope("prototype")
    public BotSender botSender(TelegRiseApplication app, InjectionPoint injectionPoint){
        return app.getResourceProvider().get(BotSender.class, injectionPoint.getMember().getDeclaringClass());
    }

    @Bean
    @Scope("prototype")
    public SessionsManager sessionsManager(TelegRiseApplication app, InjectionPoint injectionPoint){
        return app.getResourceProvider().get(SessionsManager.class, injectionPoint.getMember().getDeclaringClass() );
    }

    @Bean
    @Scope("prototype")
    public SessionMemory sessionMemory(TelegRiseApplication app, InjectionPoint injectionPoint){
        return app.getResourceProvider().get(SessionMemory.class, injectionPoint.getMember().getDeclaringClass() );
    }

    @Bean
    @Scope("prototype")
    public TranscriptionManager transcriptionManager(TelegRiseApplication app, InjectionPoint injectionPoint){
        return app.getResourceProvider().get(TranscriptionManager.class, injectionPoint.getMember().getDeclaringClass() );
    }

    @Bean
    @Scope("prototype")
    public MediaCollector mediaCollector(TelegRiseApplication app, InjectionPoint injectionPoint){
        return app.getResourceProvider().get(MediaCollector.class, injectionPoint.getMember().getDeclaringClass() );
    }
}