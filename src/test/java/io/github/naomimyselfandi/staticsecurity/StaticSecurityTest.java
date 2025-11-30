package io.github.naomimyselfandi.staticsecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StaticSecurityTest {

    @Mock
    private AnnotationMetadata importingClassMetadata;

    private StaticSecurity fixture;

    @BeforeEach
    void setup() {
        fixture = new StaticSecurity();
    }

    @Test
    void selectImports() {
        assertThat(fixture.selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration"
        );
        verifyNoInteractions(importingClassMetadata);
    }

}
