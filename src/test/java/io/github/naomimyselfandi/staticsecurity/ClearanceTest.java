package io.github.naomimyselfandi.staticsecurity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearanceTest {

    @Mock
    private Clearance clearance;

    @Mock
    private Authentication authentication;

    @Test
    void getAuthentication() {
        when(clearance.__auth__()).thenReturn(authentication);
        assertThat(Clearance.getAuthentication(clearance)).isEqualTo(authentication);
    }

}
