package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@RequiredArgsConstructor
class StaticSecurityJacksonConfiguration implements SmartInitializingSingleton {

    private final StaticSecurityModule staticSecurityModule;
    private final ObjectProvider<ObjectMapper> objectMappers;

    @Override
    public void afterSingletonsInstantiated() {
        for (var objectMapper : objectMappers) {
            objectMapper.registerModule(staticSecurityModule);
        }
    }

}
