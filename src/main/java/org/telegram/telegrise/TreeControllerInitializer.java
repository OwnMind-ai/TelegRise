package org.telegram.telegrise;

import org.telegram.telegrise.annotations.OnCreate;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.resources.ResourceInjector;

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
        Object instance = this.createInstance();

        resourceInjector.injectResources(instance);

        Optional<Method> onCreateMethod = Arrays.stream(contollerClass.getMethods())
                .filter(m -> m.isAnnotationPresent(OnCreate.class)).findFirst();

        if (onCreateMethod.isPresent()) {
            try {
                onCreateMethod.get().invoke(instance);
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException("Unable to access OnCreate method");
            } catch (InvocationTargetException e) {
                throw new TelegRiseRuntimeException("An exception occurred while creating the tree controller", e.getTargetException(), true);
            }
        }

        return instance;
    }

    private Object createInstance(){
        try {
            return contollerClass.getConstructor().newInstance();
        }  catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            String startMessage = "Cannot create instance of '" + contollerClass.getSimpleName() + "': ";

            if (e instanceof NoSuchMethodException)
                throw new TelegRiseRuntimeException(startMessage + "class must have constructor with no arguments");
            else
                throw new TelegRiseRuntimeException(startMessage + e.getMessage());
        }
    }
}
