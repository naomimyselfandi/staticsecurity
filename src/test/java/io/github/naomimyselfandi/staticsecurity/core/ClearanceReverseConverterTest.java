package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Optional;

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

    private interface SimpleClearance extends Clearance {

        TypeDescriptor TYPE = TypeDescriptor.valueOf(SimpleClearance.class);

        Foo requiredProperty();

        @SuppressWarnings("unused")
        Optional<Object> optionalProperty();

    }

    @Mock
    private SimpleClearance clearance;

    @Mock
    private Foo foo;

    @Mock
    private Bar bar;

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
        when(conversionService.canConvert(Foo.TYPE, Bar.TYPE)).thenReturn(canConvert);
        assertThat(fixture.matches(SimpleClearance.TYPE, Bar.TYPE)).isEqualTo(canConvert);
    }

    @Test
    void matches_WhenTheSourceTypeIsNotAClearanceType_ThenFalse() {
        interface NotAClearanceType {
            @SuppressWarnings("unused") Object requiredProperty();
        }
        assertThat(fixture.matches(TypeDescriptor.valueOf(NotAClearanceType.class), Bar.TYPE)).isFalse();
        verifyNoInteractions(conversionService);
    }

    @Test
    void matches_WhenTheSourceTypeIsNotASimpleClearanceType_ThenFalse() {
        interface ComplexClearance{
            @SuppressWarnings("unused") Object requiredProperty1();
            @SuppressWarnings("unused") Object requiredProperty2();
        }
        assertThat(fixture.matches(TypeDescriptor.valueOf(ComplexClearance.class), Bar.TYPE)).isFalse();
        verifyNoInteractions(conversionService);
    }

    @Test
    void convert() {
        when(clearance.requiredProperty()).thenReturn(foo);
        when(conversionService.convert(foo, Foo.TYPE, Bar.TYPE)).thenReturn(bar);
        assertThat(fixture.convert(clearance, SimpleClearance.TYPE, Bar.TYPE)).isEqualTo(bar);
    }

}
