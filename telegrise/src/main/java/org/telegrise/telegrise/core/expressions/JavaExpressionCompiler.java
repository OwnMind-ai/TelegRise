package org.telegrise.telegrise.core.expressions;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.forge.roaster.ParserException;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class JavaExpressionCompiler {
    // Change to force recompilation of expressions when incompatible changes introduced
    public static final String VERSION = "1.2.1";
    private static final String VERSION_FIELD = "VERSION";

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
    private boolean attemptRecompile = true;

    public JavaExpressionCompiler(String tempDirectoryPath) {
        this.tempDirectoryPath = new File(tempDirectoryPath);
        try {
            this.classLoader = URLClassLoader.newInstance(new URL[]{this.tempDirectoryPath.toURI().toURL()}, this.getClass().getClassLoader());
        } catch (MalformedURLException e) {
            throw new TelegRiseInternalException(e);
        }
    }

    public GeneratedValue<?> compile(String expression, LocalNamespace namespace, Class<?> returnType, Node node) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        int hashcode = this.calculateHashcode(expression, namespace);

        if (this.isExpressionExists(hashcode)){
            try{
                return createInstance(hashcode, node);
            } catch(Throwable t) {
                if (!attemptRecompile) throw t;

                // Deletes problematic file
                for (File f : Objects.requireNonNull(this.tempDirectoryPath.listFiles(((file, s) -> s.equals(className(hashcode) + ".class")))))
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();

                attemptRecompile = false;
                compile(expression, namespace, returnType, node);
            }
        }

        JavaClassSource source;
        List<Class<?>> imported;
        try {
            if (returnType == void.class)
                returnType = Void.class;
            else if (returnType.isPrimitive())
                returnType = ClassUtils.primitiveToWrapper(returnType);

            Pair<JavaClassSource, List<Class<?>>> pair = this.createSource(expression, hashcode, returnType, namespace);
            source = pair.getKey();
            imported = pair.getValue();
        } catch (ParserException e) {
            throw new TranscriptionParsingException("Syntax error in expression: " + e.getProblems().getFirst().getMessage(), node);
        }

        File sourceFile = this.createSourceFile(source, hashcode);
        this.compileSourceFile(sourceFile, node, imported);

        attemptRecompile = true;
        return createInstance(hashcode, node);
    }

    private GeneratedValue<?> createInstance(int hashcode, Node node) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        var result = (GeneratedValue<?>) this.loadExpressionClass(hashcode).getConstructor().newInstance();

        Arrays.stream(result.getClass().getFields())
                .filter(f -> f.getName().equals(VERSION_FIELD))
                .map(f -> { try { return f.get(null); } catch (Exception e) { return null; } })
                .filter(Objects::nonNull)
                .findFirst().ifPresent(
                        v -> { if (!v.equals(VERSION)) throw new TranscriptionParsingException("Wrong version of compiled expression was found: " + v, node); }
                );

        return result;
    }

    private boolean isExpressionExists(int hashcode){
        File[] files = this.tempDirectoryPath.listFiles(((file, s) -> s.equals(className(hashcode) + ".class")));
        return files != null && files.length != 0;
    }

    private Class<?> loadExpressionClass(int hashcode) throws ClassNotFoundException {
        return Class.forName(className(hashcode), true, this.classLoader);
    }

    private void compileSourceFile(File source, Node node, List<Class<?>> imported) throws IOException {
        List<String> optionList = new ArrayList<>(Arrays.asList(
                "-d", this.tempDirectoryPath.getAbsolutePath(), "-Xlint:none", "-XDsuppressNotes"
        ));

        if (System.getProperty("jdk.module.path") != null){
            optionList.addAll(Arrays.asList("--module-path", System.getProperty("jdk.module.path"),
                    "--add-modules", String.join(",", imported.stream().map(Class::getModule)
                            .map(Module::getName).filter(Objects::nonNull)
                            .map(String::strip).collect(Collectors.toSet()))));
        }

        try (StringWriter err = new StringWriter()){
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            compiler.getTask(err, fileManager, null, optionList, null,
                    fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(source))).call();
            Files.delete(source.toPath());

            if (!err.toString().isEmpty())
                throw new TranscriptionParsingException("An error occurred while compiling the expression:\n" + err, node);
        }
    }

    private File createSourceFile(JavaClassSource source, int hashcode) throws IOException {
        File result = new File(tempDirectoryPath, className(hashcode) + ".java");
        //noinspection ResultOfMethodCallIgnored
        result.getParentFile().mkdirs();
        Files.writeString(result.toPath(), source.toString());

        return result;
    }

    private Pair<JavaClassSource, List<Class<?>>> createSource(String expression, int hashcode, Class<?> returnType, LocalNamespace namespace){
        JavaClassSource source = Roaster.create(JavaClassSource.class);
        source.setName(className(hashcode));
        source.addImport(GeneratedValue.class);
        source.addImport(ResourcePool.class);
        source.addImport(returnType);
        source.addInterface(GeneratedValue.class.getSimpleName() + '<' + returnType.getSimpleName() + '>');

        source.addField().setStatic(true).setFinal(true).setPublic()
              .setType(String.class).setName(VERSION_FIELD).setLiteralInitializer("\"" + VERSION + "\"");

        String safeExpression = returnType.equals(String.class) ? "String.valueOf(" + expression + ")" : expression;
        StringBuilder builder = new StringBuilder(namespace.getResourceInitializationCode("pool"));
        builder.append("try{\n");
        if (returnType.equals(Void.class))
            builder.append(safeExpression).append(";\nreturn null;");
        else
            builder.append("return ").append(safeExpression).append(";");
        builder.append("} catch(Exception e){ throw new RuntimeException(e); }");

        MethodSource<JavaClassSource> method = source.addMethod()
                .setPublic()
                .setName(GeneratedValue.ABSTRACT_METHOD_NAME)
                .setParameters(ResourcePool.class.getSimpleName() + " pool")
                .setBody(builder.toString());

        List<Class<?>> resources = new ArrayList<>(Arrays.asList(GeneratedValue.class, ResourcePool.class, returnType));
        for (Class<?> imported : namespace.getApplicationNamespace().getImportedClasses()) {
            if (source.requiresImport(imported)) {
                source.addImport(imported);
                resources.add(imported);
            }
        }

        method.setReturnType(returnType);

        return Pair.of(source, resources);
    }

    private int calculateHashcode(String expression, LocalNamespace namespace){
        return Math.abs((
                        expression + VERSION + System.getProperty("java.version") +
                        (namespace.getHandlerClass() != null ? namespace.getHandlerClass().getName() : "") +
                        namespace.getApplicationNamespace().getImportedClasses().stream().map(Class::getName).sorted().collect(Collectors.joining())
                ).hashCode());
    }
}
