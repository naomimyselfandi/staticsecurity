package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

@Component
class JsonNodePropertyProvider implements PropertyProvider<JsonNode> {

    private static final TypeDescriptor STRING = TypeDescriptor.valueOf(String.class);
    private static final TypeDescriptor BOOLEAN = TypeDescriptor.valueOf(Boolean.class);
    private static final TypeDescriptor INTEGER = TypeDescriptor.valueOf(Integer.class);
    private static final TypeDescriptor SHORT = TypeDescriptor.valueOf(Short.class);
    private static final TypeDescriptor LONG = TypeDescriptor.valueOf(Long.class);
    private static final TypeDescriptor BIG_INTEGER = TypeDescriptor.valueOf(BigInteger.class);
    private static final TypeDescriptor BIG_DECIMAL = TypeDescriptor.valueOf(BigDecimal.class);
    private static final TypeDescriptor DOUBLE = TypeDescriptor.valueOf(Double.class);

    private final ObjectMapper objectMapper;
    private final ConversionService conversionService;

    JsonNodePropertyProvider(ObjectProvider<ObjectMapper> objectMapperProvider, ConversionService conversionService) {
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        this.conversionService = conversionService;
    }

    @Override
    public @Nullable Object extract(JsonNode source, Method property) {
        return flatten(source.get(MethodInfo.getName(property)), property);
    }

    @Override
    public @Nullable Object flatten(@Nullable JsonNode source, Method property) {
        if (source == null || source.isNull() || source.isMissingNode()) {
            return null;
        }
        var targetType = MethodInfo.getType(property);
        if (source.isTextual() && conversionService.canConvert(STRING, targetType)) {
            return conversionService.convert(source.textValue(), targetType);
        } else if (source.isBoolean() && conversionService.canConvert(BOOLEAN, targetType)) {
            return conversionService.convert(source.booleanValue(), targetType);
        } else if (source.isInt() && conversionService.canConvert(INTEGER, targetType)) {
            return conversionService.convert(source.intValue(), targetType);
        } else if (source.isShort() && conversionService.canConvert(SHORT, targetType)) {
            return conversionService.convert(source.shortValue(), targetType);
        } else if (source.isLong() && conversionService.canConvert(LONG, targetType)) {
            return conversionService.convert(source.longValue(), targetType);
        } else if (source.isIntegralNumber() && conversionService.canConvert(BIG_INTEGER, targetType)) {
            return conversionService.convert(source.bigIntegerValue(), targetType);
        } else if (source.isBigDecimal() && conversionService.canConvert(BIG_DECIMAL, targetType)) {
            return conversionService.convert(source.decimalValue(), targetType);
        } else if (source.isNumber() && conversionService.canConvert(DOUBLE, targetType)) {
            return conversionService.convert(source.doubleValue(), targetType);
        } else {
            if (source instanceof POJONode pojoNode) {
                var pojo = pojoNode.getPojo();
                if (conversionService.canConvert(TypeDescriptor.forObject(pojo), targetType)) {
                    return conversionService.convert(pojo, targetType);
                }
            }
            var javaType = resolveJavaType(targetType.getResolvableType(), objectMapper.getTypeFactory());
            try {
                return objectMapper.convertValue(source, javaType);
            } catch (IllegalArgumentException thrownByConvertValue) {
                return null;
            }
        }
    }

    private static JavaType resolveJavaType(ResolvableType type, TypeFactory typeFactory) {
        if (type.hasGenerics()) {
            var generics = type.getGenerics();
            var javaGenerics = new JavaType[generics.length];
            for (var i = 0; i < generics.length; i++) {
                javaGenerics[i] = resolveJavaType(generics[i], typeFactory);
            }
            return typeFactory.constructParametricType(type.toClass(), javaGenerics);
        } else {
            return typeFactory.constructType(type.getType());
        }
    }

}
