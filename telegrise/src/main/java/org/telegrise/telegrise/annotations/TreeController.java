package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class can be used as a controller for a tree.
 * Tree controller should have a constructor with no arguments.
 * <p>
 * After selecting the next dialog tree and creating the tree controller, annotated resources are injected, followed by invoking method annotated by <code>OnCreate</code>, if exists.
 * <p>
 * To connect tree controller to the tree transcription,
 * it should have methods annotated by {@link Reference Reference} annotation.
 * However, the framework allows calling public methods as an expression,
 * regardless of <code>Reference</code> annotation:
 * <pre>
 * {@code
 * <tag expression="${controller.method()}"/>;
 * }
 * </pre>
 *
 * After the tree is closed, the method annotated with <code>OnClose</code> will be called, if defined.
 * 
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TreeController {
    /**
     * Indicates if an annotated tree should be automatically imported into transcription.
     * If two trees have the same class name and both have {@code autoImport} as true, an error will be raised.
     */
    boolean autoImport() default true;
}
