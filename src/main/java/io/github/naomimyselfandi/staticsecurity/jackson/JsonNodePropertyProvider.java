package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

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
    public @Nullable Object extract(JsonNode source, Property property) {
        return flatten(source.get(property.name()), property);
    }

    @Override
    public @Nullable Object flatten(@Nullable JsonNode source, Property property) {
        if (source == null || source.isNull() || source.isMissingNode()) {
            return null;
        }
        var propertyType = property.type();
        if (source.isTextual() && conversionService.canConvert(STRING, propertyType)) {
            return conversionService.convert(source.textValue(), propertyType);
        } else if (source.isBoolean() && conversionService.canConvert(BOOLEAN, propertyType)) {
            return conversionService.convert(source.booleanValue(), propertyType);
        } else if (source.isInt() && conversionService.canConvert(INTEGER, propertyType)) {
            return conversionService.convert(source.intValue(), propertyType);
        } else if (source.isShort() && conversionService.canConvert(SHORT, propertyType)) {
            return conversionService.convert(source.shortValue(), propertyType);
        } else if (source.isLong() && conversionService.canConvert(LONG, propertyType)) {
            return conversionService.convert(source.longValue(), propertyType);
        } else if (source.isIntegralNumber() && conversionService.canConvert(BIG_INTEGER, propertyType)) {
            return conversionService.convert(source.bigIntegerValue(), propertyType);
        } else if (source.isBigDecimal() && conversionService.canConvert(BIG_DECIMAL, propertyType)) {
            return conversionService.convert(source.decimalValue(), propertyType);
        } else if (source.isNumber() && conversionService.canConvert(DOUBLE, propertyType)) {
            return conversionService.convert(source.doubleValue(), propertyType);
        } else {
            if (source instanceof POJONode pojoNode) {
                var pojo = pojoNode.getPojo();
                if (conversionService.canConvert(TypeDescriptor.forObject(pojo), propertyType)) {
                    return conversionService.convert(pojo, propertyType);
                }
            }
            var javaType = resolveJavaType(propertyType.getResolvableType(), objectMapper.getTypeFactory());
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
