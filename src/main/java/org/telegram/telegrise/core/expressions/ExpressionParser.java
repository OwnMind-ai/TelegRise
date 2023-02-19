package org.telegram.telegrise.core.expressions;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;

public class ExpressionParser {
    private final String tempDirectoryPath;

    public ExpressionParser(String tempDirectoryPath) {
        this.tempDirectoryPath = tempDirectoryPath;
    }

    private JavaClassSource createSource(String expression, int hashcode, Class<?> returnType, ResourcePool pool){
        JavaClassSource source = Roaster.create(JavaClassSource.class);
        source.setName("Expression" + hashcode);
        source.addImport(GeneratedValue.class);
        source.addImport(ResourcePool.class);
        source.addImport(returnType);
        source.addInterface(GeneratedValue.class.getSimpleName() + '<' + returnType.getSimpleName() + '>');

        MethodSource<JavaClassSource> method = source.addMethod()
                .setName(GeneratedValue.ABSTRACT_METHOD_NAME)
                .setParameters(ResourcePool.class.getSimpleName() + " pool")
                .setBody(pool.getResourceInitializationCode("pool") + "return " + expression + ";");

        if (returnType.equals(Void.class))
            method.setReturnTypeVoid();
        else
            method.setReturnType(returnType);

        return source;
    }
}
