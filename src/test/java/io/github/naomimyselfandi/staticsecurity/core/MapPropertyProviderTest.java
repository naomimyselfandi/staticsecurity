package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapPropertyProviderTest {

    @Mock
    private Property property;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private MapPropertyProvider fixture;

    @Test
    void extractImpl() {
        var name = UUID.randomUUID().toString();
        var value = new Object();
        when(property.name()).thenReturn(name);
        assertThat(fixture.extractImpl(Map.of(name, value), property)).isEqualTo(value);
        verifyNoInteractions(conversionService);
    }

}
