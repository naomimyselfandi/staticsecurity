package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
class AbstractProviderTest {

    private interface Source {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Source.class);
    }

    private interface Target {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Target.class);
    }

    private interface Extracted {}

    private Method property;

    @Mock
    private Source source;

    @Mock
    private Target target;

    private Extracted extracted;

    @Mock
    private ConversionService conversionService;

    private AbstractProvider<Source> fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        interface Holder {
            Target method();
        }
        property = Holder.class.getMethod("method");
        fixture = new AbstractProvider<>(conversionService) {

            @Override
            protected @Nullable Object extractImpl(@NotNull Source source, @NotNull Method property) {
                assertThat(source).isEqualTo(AbstractProviderTest.this.source);
                assertThat(property).isEqualTo(AbstractProviderTest.this.property);
                return extracted;
            }

        };
    }

    @Test
    void flatten() {
        when(conversionService.convert(source, Target.TYPE)).thenReturn(target);
        assertThat(fixture.flatten(source, property)).isEqualTo(target);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canFlatten(boolean value) {
        when(conversionService.canConvert(Source.TYPE, Target.TYPE)).thenReturn(value);
        assertThat(fixture.canFlatten(property)).isEqualTo(value);
    }

    @Test
    void extract() {
        extracted = mock();
        when(conversionService.canConvert(TypeDescriptor.forObject(extracted), Target.TYPE)).thenReturn(true);
        when(conversionService.convert(extracted, Target.TYPE)).thenReturn(target);
        assertThat(fixture.extract(source, property)).isEqualTo(target);
    }

    @Test
    void extract_WhenTheValueCannotBeConverted_ThenNull() {
        extracted = mock();
        when(conversionService.canConvert(TypeDescriptor.forObject(extracted), Target.TYPE)).thenReturn(false);
        assertThat(fixture.extract(source, property)).isNull();
        verify(conversionService, never()).convert(extracted, Target.TYPE);
    }

    @Test
    void extract_WhenNoValueIsAvailable_ThenNull() {
        assertThat(fixture.extract(source, property)).isNull();
    }

}
