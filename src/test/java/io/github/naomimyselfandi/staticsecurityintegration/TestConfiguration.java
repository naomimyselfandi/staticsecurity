package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import io.github.naomimyselfandi.staticsecurity.EnableStaticSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import static org.mockito.Mockito.*;

@Configuration
@EnableStaticSecurity
class TestConfiguration {

    @Bean
    AccessPolicy<DocumentRequest> documentRequestAccessPolicy() {
        AccessPolicy<DocumentRequest> policy = mock("documentRequestAccessPolicy");
        when(policy.getClearanceType()).thenReturn(DocumentRequest.class);
        return policy;
    }

    @Bean
    ConversionService conversionService() {
        return new DefaultFormattingConversionService();
    }

}
