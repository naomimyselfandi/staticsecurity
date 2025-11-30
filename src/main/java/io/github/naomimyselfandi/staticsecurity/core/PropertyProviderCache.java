package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class PropertyProviderCache extends Cache<Class<?>, PropertyProvider<?>> {

    private final List<PropertyProvider<?>> propertyProviders;
    private final ConversionService conversionService;

    PropertyProviderCache(List<? extends PropertyProvider<?>> propertyProviders, ConversionService conversionService) {
        this.propertyProviders = propertyProviders
                .stream()
                .sorted(Comparator.comparing(it -> -depth(it.getSourceType())))
                .collect(Collectors.toUnmodifiableList());
        this.conversionService = conversionService;
    }

    @Override
    PropertyProvider<?> calculate(Class<?> input) {
        return propertyProviders
                .stream()
                .filter(it -> it.getSourceType().isAssignableFrom(input))
                .findFirst()
                .orElseGet(() -> new ReflectivePropertyProvider<>(input, conversionService, this));
    }

    private static int depth(Class<?> type) {
        return 1 + Stream
                .concat(Stream.ofNullable(type.getSuperclass()), Arrays.stream(type.getInterfaces()))
                .mapToInt(PropertyProviderCache::depth)
                .max()
                .orElse(0);
    }

}
