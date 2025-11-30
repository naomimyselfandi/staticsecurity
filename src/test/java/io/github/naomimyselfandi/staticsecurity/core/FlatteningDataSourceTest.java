package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlatteningDataSourceTest {

    @Mock
    private PropertyProvider<Object> provider;

    private Method method;

    private FlatteningDataSource<Object> fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        interface Holder {
            Object getSomething();
        }
        method = Holder.class.getMethod("getSomething");
        fixture = new FlatteningDataSource<>(provider, method);
    }

    @Test
    void getData() {
        var source = new Object();
        var value = new Object();
        when(provider.flatten(source, method)).thenReturn(value);
        assertThat(fixture.getData(source).get()).isEqualTo(Map.of("something", value));
    }

    @Test
    void getData_WhenFlatteningReturnsNull_ThenFails() {
        var source = new Object();
        when(provider.flatten(source, method)).thenReturn(null);
        var expected = new DataSource.Failure("Required property 'something' is missing or invalid.");
        assertThat(fixture.getData(source)).isEqualTo(expected);
    }

}
