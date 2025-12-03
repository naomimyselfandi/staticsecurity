package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("EqualsWithItself")
class ClearanceInvocationHandlerTest {

    private static final Object DEFAULT = new Object();

    private interface SomeBean {}

    @Mock
    private SomeBean someBean;

    private interface TestClearance extends Clearance {

        Object getRequired();

        Optional<Object> getOptional();

        default Object getDefault() {
            return DEFAULT;
        }

        Optional<Object> getOmitted();

        OptionalInt getOmittedInt();

        OptionalLong getOmittedLong();

        OptionalDouble getOmittedDouble();

        @Helper(Helper.Type.DIRECT)
        default UUID uuidThatChangesEachTime() {
            return UUID.randomUUID();
        }

        @Helper(Helper.Type.CACHED)
        default UUID uuid() {
            return UUID.randomUUID();
        }

        @Helper(Helper.Type.CACHED)
        default String string(int prefix) {
            return "%d_%s".formatted(prefix, UUID.randomUUID());
        }

        @Helper(Helper.Type.SPRING)
        SomeBean someBean();

        void notHelper();

        void __auth__(Object parameter);

        void __data__(Object parameter);

    }

    @Mock
    private Authentication auth;

    @Mock
    private Cache<ResolvableType, Object> beans;

    @Test
    void invoke() {
        when(beans.get(any())).then(invocation -> {
            assertThat(invocation.<ResolvableType>getArgument(0).toClass()).isEqualTo(SomeBean.class);
            return someBean;
        });
        var required = new Object();
        var optional = Optional.of(new Object());
        var data = new HashMap<String, Object>();
        data.put("required", required);
        data.put("optional", optional);
        var loader = getClass().getClassLoader();
        var interfaces = new Class<?>[]{TestClearance.class};
        var handler = new ClearanceInvocationHandler(TestClearance.class, auth, data, beans);
        var clearance = (TestClearance) Proxy.newProxyInstance(loader, interfaces, handler);
        assertThat(clearance)
                .isEqualTo(clearance)
                .isNotEqualTo(mock(TestClearance.class))
                .isNotEqualTo(null)
                .returns(System.identityHashCode(clearance), TestClearance::hashCode)
                .hasToString("TestClearance(optional=%s, required=%s)", optional, required)
                .returns(required, TestClearance::getRequired)
                .returns(optional, TestClearance::getOptional)
                .returns(Optional.empty(), TestClearance::getOmitted)
                .returns(OptionalInt.empty(), TestClearance::getOmittedInt)
                .returns(OptionalLong.empty(), TestClearance::getOmittedLong)
                .returns(OptionalDouble.empty(), TestClearance::getOmittedDouble)
                .returns(DEFAULT, TestClearance::getDefault)
                .returns(data, TestClearance::__data__)
                .returns(auth, TestClearance::__auth__)
                .returns(someBean, TestClearance::someBean);
        assertThatThrownBy(clearance::notHelper)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Unsupported method")
                .hasMessageContaining("notHelper");
        assertThatThrownBy(() -> clearance.__auth__(new Object()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Unsupported method")
                .hasMessageContaining("__auth__");
        assertThatThrownBy(() -> clearance.__data__(new Object()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Unsupported method")
                .hasMessageContaining("__data__");
        assertThat(clearance.uuid()).isNotNull().isEqualTo(clearance.uuid());
        assertThat(clearance.uuidThatChangesEachTime()).isNotNull().isNotEqualTo(clearance.uuidThatChangesEachTime());
        assertThat(clearance.string(1)).isNotNull().startsWith("1_").isEqualTo(clearance.string(1));
        assertThat(clearance.string(2)).isNotNull().startsWith("2_").isEqualTo(clearance.string(2));
        assertThat(clearance.string(1)).isNotEqualTo(clearance.string(2));
    }

}
