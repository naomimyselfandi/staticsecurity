package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.Property;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearanceReverseConverterTest {

    private interface Foo {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Foo.class);
    }
    private interface Bar {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Bar.class);
    }

    private interface TestClearance extends Clearance {

        TypeDescriptor TYPE = TypeDescriptor.valueOf(TestClearance.class);

        Foo foo();

    }

    @Mock
    private TestClearance clearance;

    @Mock
    private Foo foo;

    @Mock
    private Bar bar;

    @Mock
    private Property property, anotherProperty;

    @Mock
    private Cache<Class<?>, List<Property>> propertyCache;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ClearanceReverseConverter fixture;

    @Test
    void getConvertibleTypes() {
        assertThat(fixture.getConvertibleTypes()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void matches(boolean canConvert) {
        when(propertyCache.get(TestClearance.class)).thenReturn(List.of(property, anotherProperty));
        when(property.required()).thenReturn(true);
        when(property.type()).thenReturn(Foo.TYPE);
        when(anotherProperty.required()).thenReturn(false);
        when(conversionService.canConvert(Foo.TYPE, Bar.TYPE)).thenReturn(canConvert);
        assertThat(fixture.matches(TestClearance.TYPE, Bar.TYPE)).isEqualTo(canConvert);
    }

    @Test
    void matches_WhenTheSourceTypeIsNotAClearanceType_ThenFalse() {
        interface NotAClearanceType {}
        assertThat(fixture.matches(TypeDescriptor.valueOf(NotAClearanceType.class), Bar.TYPE)).isFalse();
        verifyNoInteractions(propertyCache, conversionService);
    }

    @Test
    void matches_WhenTheSourceTypeIsNotASimpleClearanceType_ThenFalse() {
        when(propertyCache.get(TestClearance.class)).thenReturn(List.of(property, anotherProperty));
        when(property.required()).thenReturn(true);
        when(anotherProperty.required()).thenReturn(true);
        assertThat(fixture.matches(TypeDescriptor.valueOf(TestClearance.class), Bar.TYPE)).isFalse();
        verifyNoInteractions(conversionService);
    }

    @RepeatedTest(2)
    void convert(RepetitionInfo repetitionInfo) throws NoSuchMethodException {
        if (repetitionInfo.getCurrentRepetition() == 1) {
            when(propertyCache.get(TestClearance.class)).thenReturn(List.of(property));
        } else {
            when(propertyCache.get(TestClearance.class)).thenReturn(List.of(property, anotherProperty));
            when(anotherProperty.required()).thenReturn(false);
        }
        when(property.required()).thenReturn(true);
        when(property.type()).thenReturn(Foo.TYPE);
        when(property.method()).thenReturn(TestClearance.class.getMethod("foo"));
        when(clearance.foo()).thenReturn(foo);
        when(conversionService.convert(foo, Foo.TYPE, Bar.TYPE)).thenReturn(bar);
        assertThat(fixture.convert(clearance, TestClearance.TYPE, Bar.TYPE)).isEqualTo(bar);
    }

}
