package io.github.naomimyselfandi.staticsecurity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaticSecurityTest {

    @Mock
    private ClassLoader classLoader;

    @Mock
    private AnnotationMetadata importingClassMetadata;

    @Test
    void selectImports() {
        assertThat(new StaticSecurity().selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration",
                "io.github.naomimyselfandi.staticsecurity.jackson.StaticSecurityJacksonConfiguration"
        );
        verifyNoInteractions(importingClassMetadata);
    }

    @Test
    void selectImports_WhenJacksonIsNotAvailable_ThenSkipsTheIntegration() throws ClassNotFoundException {
        when(classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper"))
                .thenThrow(ClassNotFoundException.class);
        assertThat(StaticSecurity.forTesting(classLoader).selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration"
        );
        verifyNoInteractions(importingClassMetadata);
    }

}
