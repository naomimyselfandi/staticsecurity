package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import io.github.naomimyselfandi.staticsecurity.Unwrap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class ReflectivePropertyProvider<T> implements PropertyProvider<T> {

    private final List<Method> unwrappedMethods;
    private final TypeDescriptor sourceTypeDescriptor;

    @Getter(onMethod_ = @Override)
    private final Class<T> sourceType;

    final Map<String, Property> properties;
    final ConversionService conversionService;
    final Cache<Class<?>, PropertyProvider<?>> propertyProviderCache;

    ReflectivePropertyProvider(
            Class<T> sourceType,
            List<Property> properties,
            ConversionService conversionService,
            Cache<Class<?>, PropertyProvider<?>> propertyProviderCache
    ) {
        this.unwrappedMethods = Arrays
                .stream(sourceType.getMethods())
                .filter(it -> it.isAnnotationPresent(Unwrap.class))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
        this.sourceTypeDescriptor = TypeDescriptor.valueOf(sourceType);
        this.sourceType = sourceType;
        this.properties = keyByName(properties);
        this.conversionService = conversionService;
        this.propertyProviderCache = propertyProviderCache;
    }

    @Override
    public @Nullable Object extract(T source, Property property) {
        var sourceProperty = properties.get(property.name());
        if (sourceProperty != null) {
            var sourceType = sourceProperty.type();
            var targetType = property.type();
            if (conversionService.canConvert(sourceType, targetType)) {
                var method = sourceProperty.method();
                ReflectionUtils.makeAccessible(method);
                var sourceValue = ReflectionUtils.invokeMethod(method, source);
                if (sourceValue != null) {
                    return conversionService.convert(sourceValue, sourceType, targetType);
                }
            }
            return null;
        }
        for (var method : unwrappedMethods) {
            @SuppressWarnings("unchecked")
            var delegate = (PropertyProvider<Object>) (propertyProviderCache.get(method.getReturnType()));
            if (delegate.canExtract(property)) {
                ReflectionUtils.makeAccessible(method);
                var unwrap = ReflectionUtils.invokeMethod(method, source);
                if (unwrap != null) {
                    return delegate.extract(unwrap, property);
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable Object flatten(T source, Property property) {
        return conversionService.convert(source, property.type());
    }

    @Override
    public boolean canExtract(Property property) {
        var sourceProperty = properties.get(property.name());
        if (sourceProperty != null) {
            var sourceType = sourceProperty.type();
            var targetType = property.type();
            return conversionService.canConvert(sourceType, targetType);
        } else {
            return unwrappedMethods
                    .stream()
                    .map(Method::getReturnType)
                    .map(propertyProviderCache::get)
                    .anyMatch(it -> it.canExtract(property));
        }
    }

    @Override
    public boolean canFlatten(Property property) {
        return conversionService.canConvert(sourceTypeDescriptor, property.type());
    }

    private static Map<String, Property> keyByName(List<Property> properties) {
        return properties.stream().collect(Collectors.toUnmodifiableMap(Property::name, Function.identity()));
    }

}
