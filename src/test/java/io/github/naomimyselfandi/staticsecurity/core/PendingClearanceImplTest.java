package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingClearanceImplTest {

    private interface TestClearance extends Clearance {}

    @Mock
    private Authentication authentication;

    private Map<String, Object> data;

    @Mock
    private AccessPolicy<TestClearance> foo, bar;

    @Mock
    private Cache<ResolvableType, Object> beanCache;

    private PendingClearanceImpl<TestClearance> fixture;

    @BeforeEach
    void setup() {
        data = Map.of(UUID.randomUUID().toString(), new Object());
        fixture = new PendingClearanceImpl<>(TestClearance.class, data, List.of(foo, bar), beanCache);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void get(boolean nullable) {
        try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            var clearance = fixture.get(nullable);
            assertThat(clearance)
                    .isNotNull()
                    .isInstanceOf(TestClearance.class)
                    .extracting(Proxy::getInvocationHandler)
                    .asInstanceOf(InstanceOfAssertFactories.type(ClearanceInvocationHandler.class))
                    .returns(TestClearance.class, it -> it.type)
                    .returns(authentication, it -> it.auth)
                    .returns(data, it -> it.data)
                    .returns(beanCache, it -> it.beanCache);
            var inOrder = inOrder(foo, bar);
            inOrder.verify(foo).check(clearance);
            inOrder.verify(bar).check(clearance);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @RepeatedTest(2)
    void get_WhenAPolicyDeniesAccessInNullableMode_ThenReturnsNull(RepetitionInfo repetitionInfo) {
        var index = repetitionInfo.getCurrentRepetition() - 1;
        var policies = List.of(foo, bar);
        var policy = policies.get(index);
        var e = new RuntimeException();
        when(policy.check(any())).thenReturn(() -> e);
        assertThat(fixture.get(true)).isNull();
        verify(foo).check(any());
        verify(bar, times(index)).check(any());
    }

    @RepeatedTest(2)
    void get_WhenAPolicyDeniesAccessInNonNullableMode_ThenThrows(RepetitionInfo repetitionInfo) {
        var index = repetitionInfo.getCurrentRepetition() - 1;
        var policies = List.of(foo, bar);
        var policy = policies.get(index);
        var e = new RuntimeException();
        when(policy.check(any())).thenReturn(() -> e);
        assertThatThrownBy(() -> fixture.get(false)).isEqualTo(e);
        verify(foo).check(any());
        verify(bar, times(index)).check(any());
    }

}
