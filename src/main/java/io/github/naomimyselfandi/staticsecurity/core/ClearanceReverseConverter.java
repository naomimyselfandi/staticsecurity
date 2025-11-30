package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

record ClearanceReverseConverter(ConversionService conversionService) implements ConditionalGenericConverter {

    @Override
    public @Nullable Set<ConvertiblePair> getConvertibleTypes() {
        return null;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        var sourceClass = sourceType.getType();
        return Clearance.class.isAssignableFrom(sourceClass) && getSingleRequiredProperty(sourceClass)
                .map(MethodInfo::getType)
                .filter(type -> conversionService.canConvert(type, targetType))
                .isPresent();
    }

    @Override
    public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        var method = getSingleRequiredProperty(sourceType.getType()).orElseThrow();
        ReflectionUtils.makeAccessible(method);
        return Optional
                .ofNullable(source)
                .map(it -> ReflectionUtils.invokeMethod(method, it))
                .map(it -> conversionService.convert(it, MethodInfo.getType(method), targetType))
                .orElse(null);
    }

    private static Optional<Method> getSingleRequiredProperty(Class<?> type) {
        return MethodInfo
                .getProperties(type)
                .stream()
                .filter(it -> MethodRole.of(it) == MethodRole.REQUIRED)
                .limit(2)
                .map(Optional::of)
                .reduce((lhs, rhs) -> Optional.empty())
                .flatMap(Function.identity());
    }

}
