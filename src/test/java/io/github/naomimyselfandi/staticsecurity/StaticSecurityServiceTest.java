package io.github.naomimyselfandi.staticsecurity;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class StaticSecurityServiceTest {

    private interface TestSource {}

    private interface TestClearance extends Clearance {}

    private StaticSecurityService fixture;

    private final Map<Class<?>, Class<?>> canCreate = new HashMap<>();

    @BeforeEach
    void setup() {
        fixture = new StaticSecurityService() {

            @Override
            public boolean canCreate(@NotNull Class<?> source, @NotNull Class<?> type) {
                return canCreate.get(source) == type;
            }

            @Override
            public <S, C extends Clearance> @NotNull PendingClearance<C> create(@NotNull S source, @NotNull Class<C> type) {
                return fail();
            }

        };
    }

    @Test
    void createFactory() {
        canCreate.put(TestSource.class, TestClearance.class);
        assertThat(fixture.createFactory(TestSource.class, TestClearance.class))
                .asInstanceOf(InstanceOfAssertFactories.type(ClearanceFactoryImpl.class))
                .returns(TestClearance.class, it -> it.type)
                .returns(fixture, it -> it.staticSecurityService);
    }

    @Test
    void createFactory_WhenTheSourceIsInappropriate_ThenThrows() {
        assertThatThrownBy(() -> fixture.createFactory(TestSource.class, TestClearance.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create %s from %s.", TestClearance.class, TestSource.class);
    }

}
