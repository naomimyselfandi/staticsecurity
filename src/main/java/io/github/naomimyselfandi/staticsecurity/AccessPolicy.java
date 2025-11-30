package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.ResolvableType;

import java.util.function.Supplier;

/**
 * A policy that controls whether some type of access is permitted. When a
 * clearance is created, all relevant access policies are consulted, until one
 * denies access or all permit access.
 *
 * <p>Each access policy applies to some type of clearance. Access policies are
 * *covariant*: if a policy applies to some type, it applies to its subtypes as
 * well. Access policies are ordered so that more general policies are consulted
 * before more specific policies.</p>
 *
 * <p>Access policies are registered by declaring them as Spring beans.</p>
 *
 * @param <C> The type of clearance this access policy applies to.
 */
public interface AccessPolicy<C extends Clearance> {

    /**
     * Check this access policy for some clearance object. If access is denied,
     * the return value provides an exception that explains why.
     * @param clearance The clearance object to check.
     * @return {@code null} if this policy permits access, or an exception
     * supplier if this policy denies access.
     */
    @Nullable Supplier<RuntimeException> check(C clearance);

    /**
     * Get the type of clearance this access policy applies to.
     * @return The type of clearance this access policy applies to.
     */
    default Class<C> getClearanceType() {
        @SuppressWarnings("unchecked")
        var result = (Class<C>) ResolvableType.forClass(getClass()).as(AccessPolicy.class).getGeneric().toClass();
        if (result == Clearance.class) {
            var fmt = "Could not determine type information for %s. Consider overriding getClearanceType().";
            throw new IllegalStateException(fmt.formatted(getClass().getName()));
        } else {
            return result;
        }
    }

}
