package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.ResolvableType;

/**
 * A strategy for providing property values. Typically, a clearance type's
 * property values are obtained reflectively, but for some objects, such as maps
 * and Jackson {@code JsonNode}s, other strategies may be more appropriate.
 * Property providers are declared by registering them as Spring beans. If
 * multiple providers support the same source object, the most specific provider
 * is used.
 *
 * @param <S> The type of source object which this provider handles.
 */
public interface PropertyProvider<S> {

    /**
     * Extract a value for some property.
     *
     * @implSpec The returned value must be an instance of the property's type,
     * or {@code null} if a value is not available or cannot be converted to the
     * appropriate type.
     *
     * @param source The source object to extract from.
     * @param property The property for which a value is being extracted.
     * @return An instance of the property's type or {@code null}.
     */
    @Nullable Object extract(S source, Property property);

    /**
     * Flatten a source object into some property's type.
     *
     * @implSpec Implementations should <em>not</em> consider the property's
     * name. Instead, they should attempt to represent the entire source object
     * as the property's type in some way. The returned value should be an
     * instance of the property's type, or {@code null} if a value is not
     * available or cannot be converted to the appropriate type.
     *
     * @param source The source object to flatten.
     * @param property The property for which it is being flattened.
     * @return An instance of the property's type or {@code null}.
     */
    @Nullable Object flatten(S source, Property property);

    /**
     * Check if this provider can extract a property value from a source object.
     *
     * @implSpec This method does not receive a source object, which makes this
     * determination impossible for most provider implementations. In this case,
     * the implementation should simply return {@code true}, which is what the
     * default implementation does.
     *
     * @param property The property a value might be extracted for.
     * @return {@code false} if this provider definitely cannot extract a value
     * matching the property's name and type; {@code true} otherwise.
     */
    default boolean canExtract(Property property) {
        return true;
    }

    /**
     * Check if this provider can flatten a source object for some property.
     *
     * @implSpec This method does not receive a source object, which makes this
     * determination impossible for most provider implementations. In this case,
     * the implementation should simply return {@code true}, which is what the
     * default implementation does.
     *
     * @param property The property a source object might be extracted for.
     * @return {@code false} if this provider definitely cannot flatten a source
     * object into the property's type; {@code true} otherwise.
     */
    default boolean canFlatten(Property property) {
        return true;
    }

    /**
     * Get the source type which this provider handles.
     * @return The source type which this provider handles.
     */
    default Class<S> getSourceType() {
        @SuppressWarnings("unchecked")
        var result = (Class<S>) ResolvableType.forClass(getClass()).as(PropertyProvider.class).getGeneric().toClass();
        if (result == Object.class) {
            var fmt = "Could not determine type information for %s. Consider overriding getSourceType().";
            throw new IllegalStateException(fmt.formatted(getClass().getName()));
        } else {
            return result;
        }
    }

}
