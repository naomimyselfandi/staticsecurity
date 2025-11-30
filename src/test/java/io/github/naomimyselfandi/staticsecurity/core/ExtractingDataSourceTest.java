package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractingDataSourceTest {

    @Mock
    private PropertyProvider<Object> provider;

    private Method required, optional;

    private ExtractingDataSource<Object> fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        interface Holder {
            Object getRequired();
            Optional<Object> getOptional();
        }
        required = Holder.class.getMethod("getRequired");
        optional = Holder.class.getMethod("getOptional");
        fixture = new ExtractingDataSource<>(provider, List.of(optional, required));
    }

    @Test
    void getData() {
        var source = new Object();
        var val = new Object();
        var opt = Optional.of(new Object());
        when(provider.extract(source, required)).thenReturn(val);
        when(provider.extract(source, optional)).thenReturn(opt);
        assertThat(fixture.getData(source).get()).isEqualTo(Map.of("required", val, "optional", opt));
    }

    @Test
    void getData_WhenAnOptionalPropertyIsMissing_ThenSkipsIt() {
        var source = new Object();
        var val = new Object();
        when(provider.extract(source, required)).thenReturn(val);
        when(provider.extract(source, optional)).thenReturn(null);
        assertThat(fixture.getData(source).get()).isEqualTo(Map.of("required", val));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getData_WhenARequiredPropertyIsMissing_ThenFails(boolean optionalIsAvailable) {
        var source = new Object();
        when(provider.extract(source, required)).thenReturn(null);
        when(provider.extract(source, optional)).thenReturn(optionalIsAvailable ? Optional.of(new Object()) : null);
        var expected = new DataSource.Failure("Required property 'required' is missing or invalid.");
        assertThat(fixture.getData(source)).isEqualTo(expected);
    }

}
