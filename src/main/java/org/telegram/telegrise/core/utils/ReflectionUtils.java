package org.telegram.telegrise.core.utils;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.telegram.telegrise.core.parser.Element;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReflectionUtils {

    public static Class<?> getRawGenericType(Field field){
        if ( field.getGenericType() instanceof Class) return (Class<?>) field.getGenericType();

        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

        return type instanceof Class ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
    }

    @Deprecated
    public static <T> T makeImmutableElement(T object, Class<T> tClass){
        if (object == null) return null;

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        enhancer.setCallback(new GetOnlyMethodInterceptor());

        //noinspection unchecked
        return (T) enhancer.create();
    }

    @Deprecated
    private static class GetOnlyMethodInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            if (method.getName().startsWith("get")) {
                Object result = methodProxy.invoke(object, args);
                Class<?> clazz = result.getClass();

                if (clazz.isAnnotationPresent(Element.class)){
                    return makeImmutableElement(result, Object.class);
                } else if (Collection.class.isAssignableFrom(clazz)){
                    return Collections.unmodifiableCollection((Collection<?>) result);
                }

                return result;
            } else if (method.getDeclaringClass().equals(Object.class)) {
                return methodProxy.invoke(object, args);
            } else if (List.of("toString", "hashcode", "equals").contains(method.getName())) {
                return methodProxy.invoke(object, args);
            }

            throw new UnsupportedOperationException("Read-only");
        }
    }
}
