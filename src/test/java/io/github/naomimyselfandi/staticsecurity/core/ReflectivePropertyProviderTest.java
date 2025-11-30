package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import io.github.naomimyselfandi.staticsecurity.Unwrap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReflectivePropertyProviderTest {

    private interface Something {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Something.class);
    }

    private interface SomethingElse {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(SomethingElse.class);
    }

    private interface Source {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Source.class);
        SomethingElse something();
        @Unwrap UnwrappedValue getUnwrappedValue();
    }

    private interface UnwrappedValue {}

    @Mock
    private Something something;

    @Mock
    private SomethingElse somethingElse;

    @Mock
    private Source source;

    @Mock
    private PropertyProvider<UnwrappedValue> delegate;

    @Mock
    private UnwrappedValue unwrappedValue;

    private Method property, absentProperty;

    @Mock
    private ConversionService conversionService;

    @Mock
    private Cache<Class<?>, PropertyProvider<?>> propertyProviderCache;

    private ReflectivePropertyProvider<Source> fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        interface Holder {
            Something getSomething();
            boolean isSomewhere();
        }
        property = Holder.class.getMethod("getSomething");
        absentProperty = Holder.class.getMethod("isSomewhere");
        fixture = new ReflectivePropertyProvider<>(Source.class, conversionService, propertyProviderCache);
        lenient().doReturn(delegate).when(propertyProviderCache).get(UnwrappedValue.class);
    }

    @Test
    void extract() {
        when(source.something()).thenReturn(somethingElse);
        when(conversionService.canConvert(SomethingElse.TYPE, Something.TYPE)).thenReturn(true);
        when(conversionService.convert(somethingElse, SomethingElse.TYPE, Something.TYPE)).thenReturn(something);
        assertThat(fixture.extract(source, property)).isEqualTo(something);
    }

    @Test
    void extract_WhenTheValueIsNull_ThenNull() {
        when(source.something()).thenReturn(null);
        when(conversionService.canConvert(SomethingElse.TYPE, Something.TYPE)).thenReturn(true);
        assertThat(fixture.extract(source, property)).isNull();
        verify(source, never()).getUnwrappedValue();
    }

    @Test
    void extract_WhenTheTypeCannotBeConverted_ThenNull() {
        when(conversionService.canConvert(SomethingElse.TYPE, Something.TYPE)).thenReturn(false);
        assertThat(fixture.extract(source, property)).isNull();
        verify(source, never()).getUnwrappedValue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void extract_WhenThePropertyIsAbsent_ThenNull(boolean tryUnwrapping) {
        when(delegate.canExtract(absentProperty)).thenReturn(tryUnwrapping);
        assertThat(fixture.extract(source, absentProperty)).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void extract_WhenThePropertyIsAbsentButAnUnwrappedValueIsAvailable_ThenUsesIt(boolean value) {
        when(delegate.canExtract(absentProperty)).thenReturn(true);
        when(delegate.extract(unwrappedValue, absentProperty)).thenReturn(value);
        when(source.getUnwrappedValue()).thenReturn(unwrappedValue);
        assertThat(fixture.extract(source, absentProperty)).isEqualTo(value);
    }

    @Test
    void flatten() {
        when(conversionService.convert(source, Something.TYPE)).thenReturn(something);
        assertThat(fixture.flatten(source, property)).isEqualTo(something);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canExtract(boolean canConvert) {
        when(conversionService.canConvert(SomethingElse.TYPE, Something.TYPE)).thenReturn(canConvert);
        assertThat(fixture.canExtract(property)).isEqualTo(canConvert);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canExtract_WhenNoPropertyValueIsAvailable_ThenLooksFoAnUnwrappedValue(boolean value) {
        when(delegate.canExtract(absentProperty)).thenReturn(value);
        assertThat(fixture.canExtract(absentProperty)).isEqualTo(value);
        verifyNoInteractions(conversionService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canFlatten(boolean canConvert) {
        when(conversionService.canConvert(Source.TYPE, Something.TYPE)).thenReturn(canConvert);
        assertThat(fixture.canFlatten(property)).isEqualTo(canConvert);
    }

    @Test
    void getSourceType() {
        assertThat(fixture.getSourceType()).isEqualTo(Source.class);
    }

}
