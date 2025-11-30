package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessPolicyCacheTest {

    private interface Foo extends Clearance {}
    private interface Bar extends Foo {}
    private interface Baz extends Bar {}

    @Mock
    private AccessPolicy<Foo> foo;

    @Mock
    private AccessPolicy<Bar> bar;

    @Mock
    private AccessPolicy<Baz> baz;

    private AccessPolicyCache fixture;

    @BeforeEach
    void setup() {
        when(foo.getClearanceType()).thenReturn(Foo.class);
        when(bar.getClearanceType()).thenReturn(Bar.class);
        when(baz.getClearanceType()).thenReturn(Baz.class);
        var policies = new ArrayList<AccessPolicy<?>>();
        policies.add(foo);
        policies.add(bar);
        policies.add(baz);
        Collections.shuffle(policies);
        fixture = new AccessPolicyCache(policies);
    }

    @RepeatedTest(5)
    void calculate() {
        assertThat(fixture.calculate(Foo.class)).containsExactly(foo);
        assertThat(fixture.calculate(Bar.class)).containsExactly(foo, bar);
        assertThat(fixture.calculate(Baz.class)).containsExactly(foo, bar, baz);
    }

}
