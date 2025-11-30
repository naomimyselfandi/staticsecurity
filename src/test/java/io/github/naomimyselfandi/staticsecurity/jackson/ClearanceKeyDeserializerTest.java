package io.github.naomimyselfandi.staticsecurity.jackson;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.PendingClearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearanceKeyDeserializerTest {

    private interface TestClearance extends Clearance {}

    @Mock
    private PendingClearance<TestClearance> pendingClearance;

    @Mock
    private TestClearance clearance;

    @Mock
    private StaticSecurityService staticSecurityService;

    private ClearanceKeyDeserializer<TestClearance> fixture;

    @BeforeEach
    void setup() {
        fixture = new ClearanceKeyDeserializer<>(TestClearance.class, staticSecurityService);
    }

    @Test
    void deserializeKey() {
        var string = UUID.randomUUID().toString();
        when(staticSecurityService.create(string, TestClearance.class)).thenReturn(pendingClearance);
        when(pendingClearance.require()).thenReturn(clearance);
        assertThat(fixture.deserializeKey(string, null)).isEqualTo(clearance);
    }

}
