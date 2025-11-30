package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.ClearanceSourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticSecurityServiceImplTest {

    private interface Source {}
    private interface TestClearance extends Clearance {}

    @Mock
    private Source source;

    @Mock
    private DataSource<Source> dataSource;

    @Mock
    private AccessPolicy<TestClearance> foo, bar;

    @Mock
    private Authentication auth;

    @Mock
    private Cache<Class<?>, List<? extends AccessPolicy<?>>> accessPolicyCache;

    @Mock
    private Cache<ResolvableType, Object> beanCache;

    @Mock
    private Cache<DataSourceKey, Optional<? extends DataSource<?>>> dataSourceCache;

    private StaticSecurityServiceImpl fixture;

    @BeforeEach
    void setup() {
        fixture = new StaticSecurityServiceImpl(accessPolicyCache, beanCache, dataSourceCache);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canCreate(boolean expected) {
        var key = new DataSourceKey(Source.class, TestClearance.class);
        when((Object) dataSourceCache.get(key)).thenReturn(Optional.ofNullable(expected ? dataSource : null));
        assertThat(fixture.canCreate(Source.class, TestClearance.class)).isEqualTo(expected);
    }

    @Test
    void canCreate_WhenTheTypeIsNotARequestType_ThenFalse() {
        interface NotARequestType {}
        assertThat(fixture.canCreate(Source.class, NotARequestType.class)).isFalse();
        verifyNoInteractions(dataSourceCache);
    }

    @Test
    void canCreate_WhenTheTypeIsNotAnInterface_ThenFalse() {
        abstract class TestClearanceImpl implements TestClearance {}
        assertThat(fixture.canCreate(Source.class, TestClearanceImpl.class)).isFalse();
        verifyNoInteractions(dataSourceCache);
    }

    @Test
    void create() {
        try {
            var data = Map.of(UUID.randomUUID().toString(), new Object());
            var key = new DataSourceKey(source.getClass(), TestClearance.class);
            when((Object) dataSourceCache.get(key)).thenReturn(Optional.of(dataSource));
            when(dataSource.getData(source)).thenReturn(() -> data);
            when((Object) accessPolicyCache.get(TestClearance.class)).thenReturn(List.of(foo, bar));
            SecurityContextHolder.getContext().setAuthentication(auth);
            assertThat(fixture.create(source, TestClearance.class))
                    .isEqualTo(new PendingClearanceImpl<>(TestClearance.class, data, List.of(foo, bar), beanCache));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void create_WhenTheSourceIsInvalidForTheType_ThenThrows() {
        var key = new DataSourceKey(source.getClass(), TestClearance.class);
        when(dataSourceCache.get(key)).thenReturn(Optional.empty());
        when(source.toString()).thenReturn(UUID.randomUUID().toString());
        assertThatThrownBy(() -> fixture.create(source, TestClearance.class))
                .isInstanceOf(ClearanceSourceException.class)
                .hasMessage("Cannot create %s from %s.", TestClearance.class, source);
    }

}
