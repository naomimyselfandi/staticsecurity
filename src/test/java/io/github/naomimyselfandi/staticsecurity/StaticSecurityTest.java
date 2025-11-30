package io.github.naomimyselfandi.staticsecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticSecurityTest {

    @Mock
    private ClassLoader loader;

    @Mock
    private AnnotationMetadata importingClassMetadata;

    @Test
    void selectImports() {
        assertThat(new StaticSecurity().selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration",
                "io.github.naomimyselfandi.staticsecurity.jackson.StaticSecurityJacksonConfiguration",
                "io.github.naomimyselfandi.staticsecurity.web.StaticSecurityWebConfiguration"
        );
        verifyNoInteractions(importingClassMetadata);
    }

    @Test
    void selectImports_WhenJacksonIsNotAvailable_ThenSkipsTheIntegration() throws ClassNotFoundException {
        when(loader.loadClass("com.fasterxml.jackson.databind.ObjectMapper"))
                .thenThrow(ClassNotFoundException.class);
        assertThat(new StaticSecurity(loader).selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration"
        );
        verifyNoInteractions(importingClassMetadata);
        verify(loader, never()).loadClass("org.springframework.web.method.support.HandlerMethodArgumentResolver");
    }

    @Test
    void selectImports_WhenJacksonIsAvailableButWebIsNot_ThenSkipsTheWebIntegration() throws ClassNotFoundException {
        when((Object) loader.loadClass("com.fasterxml.jackson.databind.ObjectMapper"))
                .thenReturn(ObjectMapper.class);
        when(loader.loadClass("org.springframework.web.method.support.HandlerMethodArgumentResolver"))
                .thenThrow(ClassNotFoundException.class);
        assertThat(new StaticSecurity(loader).selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration",
                "io.github.naomimyselfandi.staticsecurity.jackson.StaticSecurityJacksonConfiguration"
        );
        verifyNoInteractions(importingClassMetadata);
    }

}
