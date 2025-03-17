package org.telegrise.telegrise.core;

import org.telegrise.telegrise.annotations.OnCreate;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class TreeControllerInitializer {
    private final Class<?> contollerClass;
    private final ResourceInjector resourceInjector;

    public TreeControllerInitializer(Class<?> contollerClass, ResourceInjector resourceInjector) {
        this.contollerClass = contollerClass;
        this.resourceInjector = resourceInjector;
    }

    public Object initialize(){
        Object instance = ResourceInjector.createInstance(contollerClass);
        resourceInjector.injectResources(instance);

        Optional<Method> onCreateMethod = Arrays.stream(contollerClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(OnCreate.class)).findFirst();

        onCreateMethod.ifPresent(m -> {
            try {
                m.setAccessible(true);
                m.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException("Unable to access OnCreate method");
            } catch (InvocationTargetException e) {
                throw new TelegRiseRuntimeException("An exception occurred while creating the tree controller", e.getTargetException(), true);
            }
        });

        return instance;
    }
}
