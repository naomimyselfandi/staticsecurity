package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class AccessPolicyTest {

    private interface TestClearance extends Clearance {}

    @Test
    void getClearanceType() {
        class TestPolicy implements AccessPolicy<TestClearance> {

            @Override
            public Supplier<RuntimeException> check(@NotNull AccessPolicyTest.TestClearance clearance) {
                return fail();
            }

        }
        assertThat(new TestPolicy().getClearanceType()).isEqualTo(TestClearance.class);
    }

    @Test
    void getClearanceType_WhenTypeInformationIsUnavailable_ThenThrows() {
        var policy = (AccessPolicy<TestClearance>) request -> fail();
        var fmt = "Could not determine type information for %s. Consider overriding getClearanceType().";
        assertThatThrownBy(policy::getClearanceType)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(fmt, policy.getClass().getName());
    }

}
