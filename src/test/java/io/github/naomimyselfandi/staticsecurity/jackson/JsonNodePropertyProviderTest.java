package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticsecurity.Property;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.*;
import org.springframework.core.convert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonNodePropertyProviderTest {

    private interface Something {
        JavaType JAVA_TYPE = TypeFactory.defaultInstance().constructType(Something.class);
        JavaType LIST_TYPE = TypeFactory.defaultInstance().constructParametricType(List.class, Something.class);
    }

    private static final TypeDescriptor STRING = TypeDescriptor.valueOf(String.class);
    private static final TypeDescriptor BOOLEAN = TypeDescriptor.valueOf(Boolean.class);
    private static final TypeDescriptor INTEGER = TypeDescriptor.valueOf(Integer.class);
    private static final TypeDescriptor SHORT = TypeDescriptor.valueOf(Short.class);
    private static final TypeDescriptor LONG = TypeDescriptor.valueOf(Long.class);
    private static final TypeDescriptor BIG_INTEGER = TypeDescriptor.valueOf(BigInteger.class);
    private static final TypeDescriptor BIG_DECIMAL = TypeDescriptor.valueOf(BigDecimal.class);
    private static final TypeDescriptor DOUBLE = TypeDescriptor.valueOf(Double.class);
    private static final TypeDescriptor SOMETHING = TypeDescriptor.valueOf(Something.class);

    @Mock
    private Property property;

    @Mock
    private Something something;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ConversionService conversionService;

    private JsonNodePropertyProvider fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        lenient().when(property.name()).thenReturn(UUID.randomUUID().toString());
        lenient().when(property.type()).thenReturn(SOMETHING);
        lenient().when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        var objectMapperProvider = new ObjectProvider<ObjectMapper>() {
            @Override
            public @NotNull ObjectMapper getIfAvailable(Supplier<ObjectMapper> defaultSupplier) throws BeansException {
                assertThat(defaultSupplier.get()).isExactlyInstanceOf(ObjectMapper.class);
                return objectMapper;
            }
        };
        fixture = new JsonNodePropertyProvider(objectMapperProvider, conversionService);
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void extract(JsonNode node) {
        when(objectMapper.convertValue(node, Something.JAVA_TYPE)).thenReturn(something);
        var source = new ObjectNode(JsonNodeFactory.instance, Map.of(property.name(), node));
        assertThat(fixture.extract(source, property)).isEqualTo(something);
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void flatten(JsonNode node) {
        when(objectMapper.convertValue(node, Something.JAVA_TYPE)).thenReturn(something);
        assertThat(fixture.flatten(node, property)).isEqualTo(something);
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void extract_GenericType(JsonNode node) {
        when(property.type()).thenReturn(TypeDescriptor.collection(List.class, SOMETHING));
        when(objectMapper.convertValue(node, Something.LIST_TYPE)).thenReturn(List.of(something));
        var source = new ObjectNode(JsonNodeFactory.instance, Map.of(property.name(), node));
        assertThat(fixture.extract(source, property)).isEqualTo(List.of(something));
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void flatten_GenericType(JsonNode node) {
        when(property.type()).thenReturn(TypeDescriptor.collection(List.class, SOMETHING));
        when(objectMapper.convertValue(node, Something.LIST_TYPE)).thenReturn(List.of(something));
        assertThat(fixture.flatten(node, property)).isEqualTo(List.of(something));
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void extract_WhenTheNodeCannotBeConverted_ThenNull(JsonNode node) {
        when(objectMapper.convertValue(node, Something.JAVA_TYPE)).then(invocation -> {
            var realObjectMapper = new ObjectMapper();
            return realObjectMapper.convertValue(node, Something.JAVA_TYPE); // will throw
        });
        var source = new ObjectNode(JsonNodeFactory.instance, Map.of(property.name(), node));
        assertThat(fixture.extract(source, property)).isNull();
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void flatten_WhenTheNodeCannotBeConverted_ThenNull(JsonNode node) {
        when(objectMapper.convertValue(node, Something.JAVA_TYPE)).then(invocation -> {
            var realObjectMapper = new ObjectMapper();
            return realObjectMapper.convertValue(node, Something.JAVA_TYPE); // will throw
        });
        assertThat(fixture.flatten(node, property)).isNull();
    }

    @Test
    void extract_WhenTheKeyDoesNotExist_ThenNull() {
        assertThat(fixture.extract(new ObjectNode(JsonNodeFactory.instance), property)).isNull();
    }

    @Test
    void extract_WhenTheKeyIsNull_ThenNull() {
        var source = new ObjectNode(JsonNodeFactory.instance, Map.of(property.name(), NullNode.getInstance()));
        assertThat(fixture.extract(source, property)).isNull();
    }

    @Test
    void extract_WhenTheKeyIsMissing_ThenNull() {
        var source = new ObjectNode(JsonNodeFactory.instance, Map.of(property.name(), MissingNode.getInstance()));
        assertThat(fixture.extract(source, property)).isNull();
    }

    @Test
    void flatten_WhenTheSourceIsNull_ThenNull() {
        assertThat(fixture.extract(NullNode.getInstance(), property)).isNull();
    }

    @Test
    void flatten_WhenTheSourceIsMissing_ThenNull() {
        assertThat(fixture.extract(MissingNode.getInstance(), property)).isNull();
    }

    @Test
    void extract_WhenTheSourceIsATextNode_ThenTriesTheConversionService() {
        var value = UUID.randomUUID().toString();
        when(conversionService.canConvert(STRING, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new TextNode(value)), property)).isEqualTo(something);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void extract_WhenTheSourceIsABooleanNode_ThenTriesTheConversionService(boolean value) {
        when(conversionService.canConvert(BOOLEAN, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(BooleanNode.valueOf(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAnIntNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextInt();
        when(conversionService.canConvert(INTEGER, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new IntNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAShortNode_ThenTriesTheConversionService() {
        var value = (short) ThreadLocalRandom.current().nextInt();
        when(conversionService.canConvert(SHORT, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new ShortNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAnLongNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextLong();
        when(conversionService.canConvert(LONG, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new LongNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAnBigIntNode_ThenTriesTheConversionService() {
        var value = BigInteger.valueOf(ThreadLocalRandom.current().nextLong());
        when(conversionService.canConvert(BIG_INTEGER, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new BigIntegerNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAnBigDecimalNode_ThenTriesTheConversionService() {
        var value = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());
        when(conversionService.canConvert(BIG_DECIMAL, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new DecimalNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsADoubleNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextDouble();
        when(conversionService.canConvert(DOUBLE, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new DoubleNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAFloatNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextFloat();
        when(conversionService.canConvert(DOUBLE, SOMETHING)).thenReturn(true);
        when(conversionService.convert((double) value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new FloatNode(value)), property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheSourceIsAPojoNode_ThenTriesTheConversionService() {
        var value = new Object() {};
        when(conversionService.canConvert(TypeDescriptor.forObject(value), SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.extract(wrap(new POJONode(value)), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsATextNode_ThenTriesTheConversionService() {
        var value = UUID.randomUUID().toString();
        when(conversionService.canConvert(STRING, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new TextNode(value), property)).isEqualTo(something);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void flatten_WhenTheSourceIsABooleanNode_ThenTriesTheConversionService(boolean value) {
        when(conversionService.canConvert(BOOLEAN, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(BooleanNode.valueOf(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAnIntNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextInt();
        when(conversionService.canConvert(INTEGER, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new IntNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAShortNode_ThenTriesTheConversionService() {
        var value = (short) ThreadLocalRandom.current().nextInt();
        when(conversionService.canConvert(SHORT, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new ShortNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAnLongNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextLong();
        when(conversionService.canConvert(LONG, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new LongNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAnBigIntNode_ThenTriesTheConversionService() {
        var value = BigInteger.valueOf(ThreadLocalRandom.current().nextLong());
        when(conversionService.canConvert(BIG_INTEGER, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new BigIntegerNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAnBigDecimalNode_ThenTriesTheConversionService() {
        var value = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());
        when(conversionService.canConvert(BIG_DECIMAL, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new DecimalNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsADoubleNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextDouble();
        when(conversionService.canConvert(DOUBLE, SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new DoubleNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAFloatNode_ThenTriesTheConversionService() {
        var value = ThreadLocalRandom.current().nextFloat();
        when(conversionService.canConvert(DOUBLE, SOMETHING)).thenReturn(true);
        when(conversionService.convert((double) value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new FloatNode(value), property)).isEqualTo(something);
    }

    @Test
    void flatten_WhenTheSourceIsAPojoNode_ThenTriesTheConversionService() {
        var value = new Object() {};
        when(conversionService.canConvert(TypeDescriptor.forObject(value), SOMETHING)).thenReturn(true);
        when(conversionService.convert(value, SOMETHING)).thenReturn(something);
        assertThat(fixture.flatten(new POJONode(value), property)).isEqualTo(something);
    }

    private static Stream<JsonNode> nodes() {
        return Stream.of(
                new TextNode(UUID.randomUUID().toString()),
                BooleanNode.TRUE,
                BooleanNode.FALSE,
                new ShortNode((short) ThreadLocalRandom.current().nextInt()),
                new IntNode(ThreadLocalRandom.current().nextInt()),
                new LongNode(ThreadLocalRandom.current().nextLong()),
                new BigIntegerNode(BigInteger.valueOf(ThreadLocalRandom.current().nextLong())),
                new DecimalNode(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble())),
                new DoubleNode(ThreadLocalRandom.current().nextDouble()),
                new FloatNode(ThreadLocalRandom.current().nextFloat()),
                new ArrayNode(JsonNodeFactory.instance),
                new ObjectNode(JsonNodeFactory.instance),
                new POJONode(new Object())
        );
    }

    private JsonNode wrap(JsonNode node) {
        return new ObjectNode(JsonNodeFactory.instance, Map.of(property.name(), node));
    }

}
