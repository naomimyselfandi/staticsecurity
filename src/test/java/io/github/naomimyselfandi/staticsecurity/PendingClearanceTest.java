package io.github.naomimyselfandi.staticsecurity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PendingClearanceTest {

    @Mock
    private Authentication auth1, auth2;

    @Mock
    private Clearance clearance;

    @BeforeEach
    void setup() {
        SecurityContextHolder.getContext().setAuthentication(auth1);
    }

    @AfterEach
    void teardown() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.clearContext();
        assertThat(auth).isEqualTo(auth1);
    }

    @Test
    void required() {
        assertThat(createPendingClearance(false, auth1).require()).isEqualTo(clearance);
    }

    @Test
    void optional() {
        assertThat(createPendingClearance(true, auth1).request()).contains(clearance);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void withAuth(boolean nullable) {
        assertThat(createPendingClearance(nullable, auth2).withAuth(auth2).get(nullable)).isEqualTo(clearance);
    }

    private PendingClearance<Clearance> createPendingClearance(boolean nullable, Authentication authentication) {
        return n -> {
            assertThat(n).isEqualTo(nullable);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
            return clearance;
        };
    }

}
