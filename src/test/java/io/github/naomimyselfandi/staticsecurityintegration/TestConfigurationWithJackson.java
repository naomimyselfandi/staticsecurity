package io.github.naomimyselfandi.staticsecurityintegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(TestConfiguration.class)
class TestConfigurationWithJackson {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
