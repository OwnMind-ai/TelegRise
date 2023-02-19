package org.telegram.telegrise.core.expressions;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ExpressionParser {
    private static String className(int hashcode){
        return "Expression" + hashcode;
    }

    private final File tempDirectoryPath;
    private final URLClassLoader classLoader;

    public ExpressionParser(String tempDirectoryPath) {
        this.tempDirectoryPath = new File(tempDirectoryPath);
        try {
            this.classLoader = URLClassLoader.newInstance(new URL[]{this.tempDirectoryPath.toURI().toURL()});
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public GeneratedValue<?> parse(String expression, ResourcePool pool, Class<?> returnType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        int hashcode = expression.hashCode();

        if (isExpressionExists(hashcode)) {
            return (GeneratedValue<?>) this.loadExpressionClass(hashcode).getConstructor().newInstance();
        }

        JavaClassSource source = this.createSource(expression, hashcode, returnType, pool);

        File sourceFile = this.createSourceFile(source, hashcode);
        compileSourceFile(sourceFile);

        return (GeneratedValue<?>) this.loadExpressionClass(hashcode).getConstructor().newInstance();
    }

    private boolean isExpressionExists(int hashcode){
        File[] files = this.tempDirectoryPath.listFiles(((file, s) -> s.equals(className(hashcode) + ".class")));
        return files != null && files.length != 0;
    }

    private Class<?> loadExpressionClass(int hashcode) throws ClassNotFoundException {
        return Class.forName(className(hashcode), true, this.classLoader);
    }

    private void compileSourceFile(File source) throws IOException {
        ToolProvider.getSystemJavaCompiler().run(null, null, null, source.getPath());
        Files.delete(source.toPath());
    }

    private File createSourceFile(JavaClassSource source, int hashcode) throws IOException {
        File result = new File(tempDirectoryPath, className(hashcode) + ".java");
        Files.write(result.toPath(), source.toString().getBytes(StandardCharsets.UTF_8));

        return result;
    }

    private JavaClassSource createSource(String expression, int hashcode, Class<?> returnType, ResourcePool pool){
        JavaClassSource source = Roaster.create(JavaClassSource.class);
        source.setName(className(hashcode));
        source.addImport(GeneratedValue.class);
        source.addImport(ResourcePool.class);
        source.addImport(returnType);
        source.addInterface(GeneratedValue.class.getSimpleName() + '<' + returnType.getSimpleName() + '>');

        MethodSource<JavaClassSource> method = source.addMethod()
                .setPublic()
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
