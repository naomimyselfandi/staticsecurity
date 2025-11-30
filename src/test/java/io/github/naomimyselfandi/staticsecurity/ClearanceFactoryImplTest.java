package io.github.naomimyselfandi.staticsecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearanceFactoryImplTest {

    private interface TestClearance extends Clearance {}

    @Mock
    private PendingClearance<TestClearance> testPendingClearance;

    @Mock
    private StaticSecurityService staticSecurityService;

    private ClearanceFactoryImpl<Object, TestClearance> fixture;

    @BeforeEach
    void setup() {
        fixture = new ClearanceFactoryImpl<>(TestClearance.class, staticSecurityService);
    }

    @Test
    void create() {
        var source = new Object();
        when(staticSecurityService.create(source, TestClearance.class)).thenReturn(testPendingClearance);
        assertThat(fixture.create(source)).isEqualTo(testPendingClearance);
    }

}
