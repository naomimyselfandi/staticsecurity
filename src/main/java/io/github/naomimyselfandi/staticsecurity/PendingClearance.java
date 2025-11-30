package io.github.naomimyselfandi.staticsecurity;

import org.springframework.lang.CheckReturnValue;
import org.springframework.lang.Contract;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * A clearance object that's about to be created. This interface allows the user
 * for which the clearance is issued to be overridden, and to specify how access
 * denials should be handled.
 *
 * @param <C> The type of clearance.
 */
@FunctionalInterface
public interface PendingClearance<C extends Clearance> {

    /**
     * Get the clearance, throwing on access denial.
     * @return The clearance object.
     * @throws RuntimeException on access denial. The type of exception is
     * specified by the access policy which denied access.
     */
    default C require() {
        return get(false);
    }

    /**
     * Get the clearance if access is permitted.
     * @return The clearance, or nothing if access is denied.
     */
    @CheckReturnValue
    default Optional<C> request() {
        return Optional.ofNullable(get(true));
    }

    /**
     * Specify the user to issue the clearance for. If a user is not specified,
     * the clearance is issued for the user in the security context.
     * @param authentication The user to issue the clearance for.
     * @return A new pending clearance object for that user.
     */
    @CheckReturnValue
    default PendingClearance<C> withAuth(Authentication authentication) {
        return nullable -> {
            var original = SecurityContextHolder.getContext().getAuthentication();
            try {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return get(nullable);
            } finally {
                SecurityContextHolder.getContext().setAuthentication(original);
            }
        };
    }

    /**
     * Get the clearance.

     * @apiNote Prefer to call {@link #require()} or {@link #request()} instead
     * of this method.

     * @param nullable Whether to return {@code null} on failure or throw.
     * @return The clearance.
     */
    @Contract("false -> !null")
    @Nullable C get(boolean nullable);

}
