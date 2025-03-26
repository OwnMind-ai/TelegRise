package org.telegrise.telegrise.annotations;

import org.telegrise.telegrise.UpdateHandler;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a primary handler.
 * The annotated class must implement the interface {@link UpdateHandler UpdateHandler}.
 * <p>
 * This annotation provides configuration for the handler's behavior. The parameters are:
 * <ul>
 *     <li><code>absolute</code>: if handler marked as <code>absolute</code>, it will halt update processing after handler's execution.
 *     Otherwise, the application will continue selecting handlers (absolute and not) to find a suitable tree for the update
 *     (as if no handler was used).</li>
 *     <li><code>independent</code>: if handler marked as <code>independent</code>, it will be executed before determining user session.
 *     These handlers will share state with all users that trigger it.
 *     Independent handlers can't inject {@link org.telegrise.telegrise.SessionMemory SessionMemory}.</li>
 *     <li><code>afterTrees</code>: if handler marked as <code>afterTrees</code>,
 *     it will be executed only when no tree was found to process the Update
 *     or the current tree wasn't able to process it either.
 *     (and the handler's condition was satisfied).</li>
 *     <li><code>priority</code>: indicates which handlers will be looked at first determining which will process the update (if any).
 *     Handlers with higher <code>priority</code> value will be looked at first.</li>
 * </ul>
 *
 * @see UpdateHandler
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    /**
     * If handler marked as <code>absolute</code>, it will halt update processing after handler's execution.
     * Otherwise,
     * the application will continue selecting handlers (absolute and not) to find a suitable tree for the update
     * (as if no handler was used).
     */
    boolean absolute() default true;

    /**
     * If handler marked as <code>independent</code>, it will be executed before determining user session.
     * These handlers will share state with all users that trigger it.
     * Independent handlers can't inject {@link org.telegrise.telegrise.SessionMemory SessionMemory}.
     */
    boolean independent() default false;

    /**
     * If handler marked as <code>afterTrees</code>,
     * it will be executed only when no tree was found to process the Update
     * or the current tree wasn't able to process it either.
     * (and the handler's condition was satisfied).
     */
    boolean afterTrees() default false;

    /**
     * Indicates which handlers will be looked at first determining which will process the update (if any).
     * Handlers with higher <code>priority</code> value will be looked at first.
     */
    int priority() default 0;
}
