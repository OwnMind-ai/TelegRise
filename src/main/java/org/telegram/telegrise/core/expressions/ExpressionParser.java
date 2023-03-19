package org.telegram.telegrise.core.expressions;

import org.jboss.forge.roaster.ParserException;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExpressionParser {
    public static String getTempDirectory(){
        return Objects.requireNonNullElse(
                System.getProperty("telegrise.tempDirectory"),
                System.getProperty("user.home") + "/.telegrise/temp"
        );
    }

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

    public GeneratedValue<?> parse(String expression, LocalNamespace namespace, Class<?> returnType, Node node) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        int hashcode = this.calculateHashcode(expression, namespace);  //TODO cleaning unused compiled expressions

        if (isExpressionExists(hashcode)) {
            return (GeneratedValue<?>) this.loadExpressionClass(hashcode).getConstructor().newInstance();
        }

        JavaClassSource source;
        try {
            source = this.createSource(expression, hashcode, returnType, namespace);
        } catch (ParserException e) {
            throw new TranscriptionParsingException("Syntax error in expression: " + e.getProblems().get(0).getMessage(), node);
        }

        File sourceFile = this.createSourceFile(source, hashcode);
        compileSourceFile(sourceFile, node);

        return (GeneratedValue<?>) this.loadExpressionClass(hashcode).getConstructor().newInstance();
    }

    private boolean isExpressionExists(int hashcode){
        File[] files = this.tempDirectoryPath.listFiles(((file, s) -> s.equals(className(hashcode) + ".class")));
        return files != null && files.length != 0;
    }

    private Class<?> loadExpressionClass(int hashcode) throws ClassNotFoundException {
        return Class.forName(className(hashcode), true, this.classLoader);
    }

    private void compileSourceFile(File source, Node node) throws IOException {
        try (ByteArrayOutputStream err = new ByteArrayOutputStream()){
            ToolProvider.getSystemJavaCompiler().run(null, null, err, source.getPath());
            Files.delete(source.toPath());

            if (err.size() > 0)
                throw new TranscriptionParsingException("An error occurred while compiling the expression:\n" + err, node);
        }
    }

    private File createSourceFile(JavaClassSource source, int hashcode) throws IOException {
        File result = new File(tempDirectoryPath, className(hashcode) + ".java");
        //noinspection ResultOfMethodCallIgnored
        result.getParentFile().mkdirs();
        Files.write(result.toPath(), source.toString().getBytes(StandardCharsets.UTF_8));

        return result;
    }

    private JavaClassSource createSource(String expression, int hashcode, Class<?> returnType, LocalNamespace namespace){
        JavaClassSource source = Roaster.create(JavaClassSource.class);
        source.setName(className(hashcode));
        source.addImport(GeneratedValue.class);
        source.addImport(ResourcePool.class);
        source.addImport(returnType);
        source.addInterface(GeneratedValue.class.getSimpleName() + '<' + returnType.getSimpleName() + '>');

        String safeExpression = returnType.equals(String.class) ? "String.valueOf(" + expression + ")" : expression;
        MethodSource<JavaClassSource> method = source.addMethod()
                .setPublic()
                .setName(GeneratedValue.ABSTRACT_METHOD_NAME)
                .setParameters(ResourcePool.class.getSimpleName() + " pool")
                .setBody(namespace.getResourceInitializationCode("pool") + "return " + safeExpression + ";");

        for (Class<?> imported : namespace.getApplicationNamespace().getImportedClasses())
            if (source.requiresImport(imported))
                source.addImport(imported);

        if (returnType.equals(Void.class))
            method.setReturnTypeVoid();
        else
            method.setReturnType(returnType);

        return source;
    }

    private int calculateHashcode(String expression, LocalNamespace namespace){
        return Math.abs((
                        expression +
                        (namespace.getHandlerClass() != null ? namespace.getHandlerClass().getName() : "") +
                        namespace.getApplicationNamespace().getImportedClasses().stream().map(Class::getName).sorted().collect(Collectors.joining())
                ).hashCode());
    }
}
