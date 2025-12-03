package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlatteningDataSourceTest {

    @Mock
    private PropertyProvider<Object> provider;

    @Mock
    private Property property;

    private FlatteningDataSource<Object> fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        when(property.name()).thenReturn(UUID.randomUUID().toString());
        fixture = new FlatteningDataSource<>(provider, property);
    }

    @Test
    void getData() {
        var source = new Object();
        var value = new Object();
        when(provider.flatten(source, property)).thenReturn(value);
        assertThat(fixture.getData(source).get()).isEqualTo(Map.of(property.name(), value));
    }

    @Test
    void getData_WhenFlatteningReturnsNull_ThenFails() {
        var source = new Object();
        when(provider.flatten(source, property)).thenReturn(null);
        var reason = "Required property '%s' is missing or invalid.".formatted(property.name());
        var expected = new DataSource.Failure(reason);
        assertThat(fixture.getData(source)).isEqualTo(expected);
    }

}
