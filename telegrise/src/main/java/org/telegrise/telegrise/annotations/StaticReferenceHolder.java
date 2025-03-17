package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that a singleton instance of an annotated class can be used to provide static method references.
 * References declared in the holder can be accessed in the same way as static references are.
 * <p>
 * At the startup of the application, a singleton is created once and acts as an alternative to conventional static reference declaration:
 * <pre>
 * {@code
 * public class Holder {
 *     @Reference
 *     public static void method() { ... }
 * }
 * // OR
 * @StaticReferenceHolder
 * public class Holder {
 *     @Reference
 *     public void method() { ... }
 * }
 * }
 * </pre>
 * Such reference holders are useful when they have to be initialized by a third-party lifecycle mechanism,
 * such as Spring Boot's bean proxies and JPA transactional methods.
 *
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StaticReferenceHolder {
}
