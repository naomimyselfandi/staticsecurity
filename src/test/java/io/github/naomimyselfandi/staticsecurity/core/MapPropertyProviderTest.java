package io.github.naomimyselfandi.staticsecurity.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapPropertyProviderTest {

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private MapPropertyProvider fixture;

    @Test
    void extractImpl() throws NoSuchMethodException {
        interface Holder {
            Object getSomething();
        }
        var value = new Object();
        var property = Holder.class.getMethod("getSomething");
        assertThat(fixture.extractImpl(Map.of("something", value), property)).isEqualTo(value);
        verifyNoInteractions(conversionService);
    }

}
