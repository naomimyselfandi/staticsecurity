package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticSecurityJacksonConfigurationTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StaticSecurityModule staticSecurityModule;

    @Mock
    private ObjectProvider<ObjectMapper> objectMappers;

    @InjectMocks
    private StaticSecurityJacksonConfiguration fixture;

    @Test
    void afterSingletonsInstantiated() {
        when(objectMappers.iterator()).then(invocation -> List.of(objectMapper).iterator());
        fixture.afterSingletonsInstantiated();
        verify(objectMapper).registerModule(staticSecurityModule);
    }

}
