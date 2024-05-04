package org.telegram.telegrise.caching;

/**
 * Enum of caching strategies for method references. Cache in the context of the method references is a stored
 * return value of the method that is going to be reused instead of invoking the method itself. This greatly increases
 * performance and code reusability, especially when working with constructions like this:
 * <pre>
 *     &lt;branch when="#extractFromUpdate -&gt; #notNull" invoke="#extractFromUpdate -> #doSomething"/&gt;
 * </pre>
 * Currently implemented strategies are:
 * <ul>
 *     <li>NONE (default): Disables caching completely</li>
 *     <li>UPDATE: Keeps cached value while the tree controller handles <b>the latest update</b> (relies on update's ID)</li>
 *     <li>BRANCH: Keeps cached value while working within specific <b>branch</b> where method was initially invoked</li>
 *     <li>TREE: Keeps cached value while working within specific <b>tree</b> where method was initially invoked</li>
 * </ul>
 *
 * @since 0.6
 * @see org.telegram.telegrise.annotations.Reference
 */
// TODO include passage about forced caching when implemented
public enum CachingStrategy {
    NONE, UPDATE, BRANCH, TREE
}
