package org.telegrise.telegrise.starter;

import lombok.Setter;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.*;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.ResourceInjector;
import org.telegrise.telegrise.core.utils.ReflectionUtils;
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
    public ApplicationRunner telegRiseRunner(TelegRiseApplication app, GenericApplicationContext context){
        context.getBeansOfType(Service.class).values().forEach(app::addService);
        context.getBeansOfType(SessionInitializer.class).values().stream().findFirst().ifPresent(app::setSessionInitializer);
        context.getBeansOfType(RoleProvider.class).values().stream().findFirst().ifPresent(app::setRoleProvider);
        context.getBeansOfType(TelegRiseExecutorService.class).values().stream().findFirst()
                .ifPresent(e -> app.setExecutorService(() -> e));

        TelegRiseSessionScope scope = new TelegRiseSessionScope();
        context.getBeanFactory().registerScope(TelegRiseSessionScope.NAME, scope);
        app.getSessionManager().registerSessionDestructionCallback((i, m) -> scope.destroySession(i));

        registerApplicationBeans(context);

        ResourceInjector.setInstanceInitializer(context::getBean);
        ReflectionUtils.setClassGetter(AopProxyUtils::ultimateTargetClass);

        return args -> app.start();
    }

    private void registerApplicationBeans(GenericApplicationContext context) {
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(TreeController.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(Handler.class));

        for (BeanDefinition definition : provider.findCandidateComponents(getApplicationClass(context).getPackageName())){
            if (definition.getBeanClassName() == null) continue;

            definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            context.registerBeanDefinition(definition.getBeanClassName(), definition);
        }
    }

    @Bean
    public TelegramClient telegramClient(TelegRiseApplication app){
        return app.getTelegramClient();
    }

    @Bean(name = "org.telegrise.telegrise.senders.BotSender")
    @Scope(TelegRiseSessionScope.NAME)
    public BotSender botSender(TelegramClient client){
        return new BotSender(client, null);
    }

    @Bean(name = "org.telegrise.telegrise.TranscriptionManager")
    @Scope(TelegRiseSessionScope.NAME)
    public TranscriptionManager transcriptionManager(TelegRiseApplication app){
        return app.getSessionManager().getTranscriptionManager();
    }

    @Bean(name = "org.telegrise.telegrise.SessionMemory")
    @DependsOn("telegRiseApplication")
    @Scope(TelegRiseSessionScope.NAME)
    public SessionMemory sessionMemory(){
        return null;
    }

    @Bean(name = "org.telegrise.telegrise.MediaCollector")
    @DependsOn("telegRiseApplication")
    @Scope(TelegRiseSessionScope.NAME)
    public MediaCollector mediaCollector(){
        return null;
    }

    @Bean
    public SessionsManager sessionsManager(TelegRiseApplication app){
        return app.getSessionManager();
    }

    @Bean
    public BotUser botUser(TelegRiseApplication app){
        return app.getBotUser();
    }
}