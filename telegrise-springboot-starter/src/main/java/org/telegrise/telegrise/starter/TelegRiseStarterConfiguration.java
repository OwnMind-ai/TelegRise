package org.telegrise.telegrise.starter;

import lombok.Setter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.*;
import org.telegrise.telegrise.core.ResourceInjector;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.types.BotUser;

import java.io.File;

@Setter
@Configuration
@ComponentScan
@EnableConfigurationProperties(TelegRiseProperties.class)
public class TelegRiseStarterConfiguration {
    @Bean
    public TelegRiseApplication telegRiseApplication(ConfigurableApplicationContext context) {
        var transcription = context.getEnvironment().getProperty("telegrise.transcription");
        if (transcription == null || !(new File(transcription).exists()))
            throw new TelegRiseRuntimeException("No transcription file specified or file doesn't exists. Please, add 'telegrise.transcription'" +
                    " to the application properties with a valid path to the transcription file.");

        var app = new TelegRiseApplication(new File(transcription), this.getApplicationClass(context));

        app.preload();
        return app;
    }

    private Class<?> getApplicationClass(ApplicationContext context) {
        return context.getBeansWithAnnotation(SpringBootApplication.class).values().stream()
                .map(o -> (Class<?>) o.getClass()).findFirst()
                .orElseThrow(() -> new TelegRiseRuntimeException("No @SpringBootApplication class was found"));
    }

    @Bean
    public ApplicationRunner telegRiseRunner(TelegRiseApplication app, ApplicationContext context, TelegRiseSessionContextProvider contextProvider){
        context.getBeansOfType(Service.class).values().forEach(app::addService);
        context.getBeansOfType(SessionInitializer.class).values().stream().findFirst().ifPresent(app::setSessionInitializer);
        context.getBeansOfType(RoleProvider.class).values().stream().findFirst().ifPresent(app::setRoleProvider);
        context.getBeansOfType(TelegRiseExecutorService.class).values().stream().findFirst()
                .ifPresent(e -> app.setExecutorService(() -> e));

        ResourceInjector.setInstanceInitializer(contextProvider::getBean);

        return args -> app.start();
    }

    @Bean
    @DependsOn("telegRiseApplication")
    public TelegramClient telegramClient(TelegRiseApplication app){
        return app.getTelegramClient();
    }

    @Bean
    @DependsOn("telegRiseApplication")
    public BotSender botSender(TelegramClient client){
        return new BotSender(client, null);
    }

    @Bean
    @DependsOn("telegRiseApplication")
    public SessionsManager sessionsManager(TelegRiseApplication app){
        return app.getSessionManager();
    }

    @Bean
    @DependsOn("telegRiseApplication")
    public BotUser botUser(TelegRiseApplication app){
        return app.getBotUser();
    }

    @Bean
    @DependsOn("telegRiseApplication")
    public TranscriptionManager transcriptionManager(TelegRiseApplication app){
        return app.getSessionManager().getTranscriptionManager();
    }
}