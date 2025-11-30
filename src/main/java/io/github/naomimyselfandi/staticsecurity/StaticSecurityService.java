package io.github.naomimyselfandi.staticsecurity;

/**
 * The core of the static security library. Using this service directly is rare,
 * unusual, as higher-level abstractions are available which provide additional
 * functionality.
 */
public interface StaticSecurityService {

    /**
     * Check if some type can be converted to a clearance type.
     *
     * @param source The source type.
     * @param type The clearance type.
     * @return Whether the source type can be converted to the clearance type.
     */
    boolean canCreate(Class<?> source, Class<?> type);

    /**
     * Create a clearance object from some source object.
     *
     * @apiNote Similar functionality is available through Spring's conversion
     * service. {@code conversionService.convert(source, type)} is equivalent to
     * {@code staticSecurityService.convert(source, type).require()}.
     *
     * @param source The source object.
     * @param type The type of clearance to create.
     * @return A {@link PendingClearance} object to specify the user and how to
     * handle denials.
     * @param <S> The type of the source object.
     * @param <C> The type of clearance to create.
     * @throws ClearanceSourceException if the source object is not
     * appropriate for the clearance type.
     */
    <S, C extends Clearance> PendingClearance<C> create(S source, Class<C> type);

    /**
     * Create a factory for some type of clearance.
     *
     * @apiNote Clearance factories can also be autowired, which calls this
     * method internally.
     *
     * @param source The type the factory creates clearances from.
     * @param type The type of clearance the factory creates.
     * @return A clearance factory.
     * @param <S> The type the factory creates clearances from.
     * @param <C> The type of clearance the factory creates.
     */
    default <S, C extends Clearance> ClearanceFactory<S, C> createFactory(Class<S> source, Class<C> type) {
        if (canCreate(source, type)) {
            return new ClearanceFactoryImpl<>(type, this);
        } else {
            throw new IllegalArgumentException("Cannot create %s from %s.".formatted(type, source));
        }
    }

}
