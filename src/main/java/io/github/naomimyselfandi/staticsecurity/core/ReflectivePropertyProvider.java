package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.MethodInfo;
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

    private final Map<String, Method> sourceProperties;

    private final List<Method> unwrappedMethods;

    @Getter(onMethod_ = @Override)
    private final Class<T> sourceType;
    private final ConversionService conversionService;
    private final Cache<Class<?>, PropertyProvider<?>> propertyProviderCache;

    ReflectivePropertyProvider(
            Class<T> sourceType,
            ConversionService conversionService,
            Cache<Class<?>, PropertyProvider<?>> propertyProviderCache
    ) {
        this.sourceProperties = MethodInfo
                .getProperties(sourceType)
                .stream()
                .peek(ReflectionUtils::makeAccessible)
                .collect(Collectors.toUnmodifiableMap(MethodInfo::getName, Function.identity()));
        this.unwrappedMethods = Arrays
                .stream(sourceType.getMethods())
                .filter(it -> it.isAnnotationPresent(Unwrap.class))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
        this.sourceType = sourceType;
        this.conversionService = conversionService;
        this.propertyProviderCache = propertyProviderCache;
    }

    @Override
    public @Nullable Object extract(T source, Method property) {
        var sourceProperty = sourceProperties.get(MethodInfo.getName(property));
        if (sourceProperty != null) {
            var sourceType = MethodInfo.getType(sourceProperty);
            var targetType = MethodInfo.getType(property);
            if (conversionService.canConvert(sourceType, targetType)) {
                var sourceValue = ReflectionUtils.invokeMethod(sourceProperty, source);
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
    public @Nullable Object flatten(T source, Method property) {
        return conversionService.convert(source, MethodInfo.getType(property));
    }

    @Override
    public boolean canExtract(Method property) {
        var sourceProperty = sourceProperties.get(MethodInfo.getName(property));
        if (sourceProperty != null) {
            var sourceType = MethodInfo.getType(sourceProperty);
            var targetType = MethodInfo.getType(property);
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
    public boolean canFlatten(Method property) {
        return conversionService.canConvert(TypeDescriptor.valueOf(sourceType), MethodInfo.getType(property));
    }

}
