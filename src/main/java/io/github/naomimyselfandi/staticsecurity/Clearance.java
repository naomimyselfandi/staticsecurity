package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.Unmodifiable;
import org.springframework.security.core.Authentication;

import java.lang.annotation.*;
import java.util.Map;

/**
 * A request for which access checks have been performed. Each subtype of this
 * interface represents a different kind of request, and may declare properties
 * and helper methods as needed. If an application's sensitive service methods
 * declare parameters of clearance types, there is a strong, statically checked
 * guarantee that all calls to those methods have performed access checks.
 *
 * <p>Unless specified otherwise, a parameterless, non-{@code void} method
 * declared in a clearance type is assumed to define a property. If the method
 * has a {@code default} implementation or its return type is an {@code Optional}
 * type, {@code OptionalInt}, {@code OptionalLing}, or {@code OptionalDouble},
 * the property is optional; otherwise, it is required. Clearance interfaces are
 * instantiated by mapping the properties of a <em>source object</em> to the
 * clearance interface's properties. If a method's name begins with {@code get}
 * or {@code is} followed by a capital letter, the property's name is the method
 * name without that prefix and with the initial capital converted to lower
 * case; it is the same as the method name otherwise.</p>
 *
 * <p>Method names beginning and ending with double underscores are reserved for
 * internal use. They do not define not properties. Methods inherited from
 * {@link Object} are also not properties, even if they are overridden in a
 * clearance interface (perhaps to add annotations). In the unusual case of a
 * clearance interface extending a non-clearance interface, methods inherited
 * from the non-clearance interface define properties if they have the correct
 * signature to do so.</p>
 */
public interface Clearance {

    /**
     * Get the user for whom a clearance was issued.
     * @implSpec This is an alias for {@link #__auth__()}.
     * @param clearance The clearance.
     * @return The user for whom the clearance was issued.
     */
    static Authentication getAuthentication(Clearance clearance) {
        return clearance.__auth__();
    }

    /**
     * Configure the annotated method as a <em>helper method</em>. A helper
     * method never defines property, ensuring that they cannot be accidentally
     * overwritten during property mapping.
     * @see Clearance.Helper.Type
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Helper {

        /**
         * A type of helper method.
         */
        enum Type {

            /**
             * A marker for direct helper methods. A direct helper method
             * provides functionality that may be useful in access checks; this
             * functionality is specified in a {@code default} implementation.
             * Direct helper methods may accept parameters and may have any
             * return type, including {@code void}.
             */
            DIRECT,

            /**
             * A marker for cached helper methods. A cached helper method is
             * similar to a {@linkplain #DIRECT direct helper method}, except
             * that it is automatically memoized. If the method has parameters,
             * the memoization logic takes them into account.
             *
             * <p>Cached helpers may be used to fetch database records, ensuring
             * that they are not read more often than necessary. Consider using
             * a {@linkplain #SPRING Spring helper} to provide access to the
             * repository in this case.</p>
             */
            CACHED,

            /**
             * A marker for Spring helper methods. A Spring helper method
             * provides access to a Spring bean, specified through its return
             * type. Spring helpers must not have parameters or {@code default}
             * implementations.
             */
            SPRING,

        }

        /**
         * Specify the helper method type.
         * @return The helper method type.
         */
        Type value();

    }

    /**
     * Get the user for whom this clearance was issued.
     * @apiNote This method is named to avoid conflicts with user-defined
     * methods. Consider using {@link #getAuthentication(Clearance)}, or
     * declaring a {@linkplain Helper helper method} that delegates to this
     * method if you'd prefer instance method syntax.
     * @return The user for whom this clearance was issued.
     */
    Authentication __auth__();

    /**
     * View this clearance object as a map.
     * @apiNote This method is named to avoid conflicts with user-defined
     * methods. Client code may call this, but should do so rarely.
     * @return This clearance object's values.
     */
    @Unmodifiable Map<String, Object> __data__();

}
