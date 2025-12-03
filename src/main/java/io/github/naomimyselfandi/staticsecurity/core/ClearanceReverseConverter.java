package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.Property;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.ReflectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

record ClearanceReverseConverter(
        Cache<Class<?>, List<Property>> propertyCache,
        ConversionService conversionService
) implements ConditionalGenericConverter {

    @Override
    public @Nullable Set<ConvertiblePair> getConvertibleTypes() {
        return null;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        var sourceClass = sourceType.getType();
        return Clearance.class.isAssignableFrom(sourceClass) && getSingleRequiredProperty(sourceClass)
                .map(Property::type)
                .filter(type -> conversionService.canConvert(type, targetType))
                .isPresent();
    }

    @Override
    public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        var property = getSingleRequiredProperty(sourceType.getType()).orElseThrow();
        var method = property.method();
        ReflectionUtils.makeAccessible(method);
        return Optional
                .ofNullable(source)
                .map(it -> ReflectionUtils.invokeMethod(method, it))
                .map(it -> conversionService.convert(it, property.type(), targetType))
                .orElse(null);
    }

    private Optional<Property> getSingleRequiredProperty(Class<?> type) {
        return propertyCache
                .get(type)
                .stream()
                .filter(Property::required)
                .limit(2)
                .map(Optional::of)
                .reduce((lhs, rhs) -> Optional.empty())
                .flatMap(Function.identity());
    }

}
