package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractingDataSourceTest {

    @Mock
    private PropertyProvider<Object> provider;

    @Mock
    private Property required, optional;

    private ExtractingDataSource<Object> fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        lenient().when(required.name()).thenReturn(UUID.randomUUID().toString());
        lenient().when(optional.name()).thenReturn(UUID.randomUUID().toString());
        lenient().when(required.required()).thenReturn(true);
        lenient().when(optional.required()).thenReturn(false);
        fixture = new ExtractingDataSource<>(provider, List.of(optional, required));
    }

    @Test
    void getData() {
        var source = new Object();
        var val = new Object();
        var opt = Optional.of(new Object());
        when(provider.extract(source, required)).thenReturn(val);
        when(provider.extract(source, optional)).thenReturn(opt);
        assertThat(fixture.getData(source).get()).isEqualTo(Map.of(required.name(), val, optional.name(), opt));
    }

    @Test
    void getData_WhenAnOptionalPropertyIsMissing_ThenSkipsIt() {
        var source = new Object();
        var val = new Object();
        when(provider.extract(source, required)).thenReturn(val);
        when(provider.extract(source, optional)).thenReturn(null);
        assertThat(fixture.getData(source).get()).isEqualTo(Map.of(required.name(), val));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getData_WhenARequiredPropertyIsMissing_ThenFails(boolean optionalIsAvailable) {
        var source = new Object();
        when(provider.extract(source, required)).thenReturn(null);
        when(provider.extract(source, optional)).thenReturn(optionalIsAvailable ? Optional.of(new Object()) : null);
        var reason = "Required property '%s' is missing or invalid.".formatted(required.name());
        var expected = new DataSource.Failure(reason);
        assertThat(fixture.getData(source)).isEqualTo(expected);
    }

}
