package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;

/**
 * An abstract property provider implementation. This class implements source
 * object flattening by delegating to Spring's conversion service, which is
 * appropriate for most providers, and provides a partial implementation of
 * {@link #extract(Object, Property)} which converts extracted values.
 *
 * @param <S> The type of source object which this provider handles.
 */
public abstract class AbstractProvider<S> implements PropertyProvider<S> {

    protected final ConversionService conversionService;
    private final TypeDescriptor sourceType;

    protected AbstractProvider(ConversionService conversionService) {
        this.conversionService = conversionService;
        this.sourceType = TypeDescriptor.valueOf(getSourceType());
    }

    @Override
    public @Nullable Object flatten(S source, Property property) {
        return conversionService.convert(source, property.type());
    }

    @Override
    public @Nullable Object extract(S source, Property property) {
        var value = extractImpl(source, property);
        if (value == null) {
            return null;
        }
        var valueType = TypeDescriptor.forObject(value);
        var propertyType = property.type();
        if (conversionService.canConvert(valueType, propertyType)) {
            return conversionService.convert(value, propertyType);
        } else {
            return null;
        }
    }

    @Override
    public boolean canFlatten(Property property) {
        return conversionService.canConvert(sourceType, property.type());
    }

    /**
     * Extract a value for some property.
     *
     * @implNote Implementations are not responsible for checking the property's
     * type unless they can provide conversions beyond those offered by Spring's
     * conversion service.
     *
     * @param source The source object to extract from.
     * @param property The property for which a value is being extracted.
     * @return The extracted object (of any type) or {@code null}.
     */
    protected abstract @Nullable Object extractImpl(S source, Property property);

}
