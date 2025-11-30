package io.github.naomimyselfandi.staticsecurity;

/**
 * A factory that creates some type of clearance. Clearance factories can be
 * autowired; if the source type is not appropriate for the clearance type, the
 * autowiring fails, providing early feedback.
 *
 * @param <S> The source type to create clearances from.
 * @param <C> The type of clearance to create.
 */
@FunctionalInterface
public interface ClearanceFactory<S, C extends Clearance> {

    /**
     * Create a clearance from a source object.
     *
     * @param source The source object.
     * @return A {@link PendingClearance} object to specify the user and how to
     * handle denials.
     */
    PendingClearance<C> create(S source);

}
