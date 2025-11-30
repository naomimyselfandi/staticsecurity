package io.github.naomimyselfandi.staticsecurity;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;

/**
 * An abstract property provider implementation. This class implements source
 * object flattening by delegating to Spring's conversion service, which is
 * appropriate for most providers, and provides a partial implementation of
 * {@link #extract(Object, Method)} which converts extracted values.
 *
 * @param <S> The type of source object which this provider handles.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractProvider<S> implements PropertyProvider<S> {

    protected final ConversionService conversionService;

    @Override
    public @Nullable Object flatten(S source, Method property) {
        return conversionService.convert(source, MethodInfo.getType(property));
    }

    @Override
    public @Nullable Object extract(S source, Method property) {
        var value = extractImpl(source, property);
        if (value == null) {
            return null;
        }
        var valueType = TypeDescriptor.forObject(value);
        var propertyType = MethodInfo.getType(property);
        if (conversionService.canConvert(valueType, propertyType)) {
            return conversionService.convert(value, propertyType);
        } else {
            return null;
        }
    }

    @Override
    public boolean canFlatten(Method property) {
        return conversionService.canConvert(TypeDescriptor.valueOf(getSourceType()), MethodInfo.getType(property));
    }

    /**
     * Extract a value for some property.
     *
     * @implSpec Implementations should use {@link MethodInfo#getName(Method)}
     * to determine property's name, since it may be different from the method
     * name. Implementations are not responsible for checking the property's
     * type unless they can provide conversions beyond those offered by Spring's
     * conversion service.
     *
     * @param source The source object to extract from.
     * @param property The property for which a value is being extracted.
     * @return The extracted object (of any type) or {@code null}.
     */
    protected abstract @Nullable Object extractImpl(S source, Method property);

}
