package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableWebMvc
@Configuration
@ComponentScan
@Import(TestConfigurationWithJackson.class)
class TestConfigurationWithMockMvc {

    @Bean
    AccessPolicy<DocumentRequest> documentRequestAccessPolicy() {
        AccessPolicy<DocumentRequest> policy = mock("documentRequestAccessPolicy");
        when(policy.getClearanceType()).thenReturn(DocumentRequest.class);
        return policy;
    }

}
