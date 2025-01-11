package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class can be used as a controller for a tree.
 * Tree controller should have a constructor with no arguments.
 * <p>
 * After selecting the next dialog tree and creating the tree controller, annotated resources are injected, followed by invoking method annotated by <code>OnCreate</code>, if exists.
 * </p><p>
 * In order to connect tree controller to the tree transcription, it should have methods annotated by <a href="#{@link}">{@link Reference Reference}</a> annotation.
 * However, framework allows calling public methods as an expression, regardless of <code>Reference</code> annotation:
 * <pre>
 * &lt;tag expression="${controller.method()}"/&gt;
 * </pre>
 * </p>
 * After the tree is closed, the method annotated with <code>OnClose</code> will be called, if exists.
 * 
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TreeController {
}
