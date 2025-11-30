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

    private interface TestRequest extends Clearance {

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
        var interfaces = new Class<?>[]{TestRequest.class};
        var handler = new ClearanceInvocationHandler(TestRequest.class, auth, data, beans);
        var request = (TestRequest) Proxy.newProxyInstance(loader, interfaces, handler);
        assertThat(request)
                .isEqualTo(request)
                .isNotEqualTo(mock(TestRequest.class))
                .isNotEqualTo(null)
                .returns(System.identityHashCode(request), TestRequest::hashCode)
                .hasToString("TestRequest(optional=%s, required=%s)", optional, required)
                .returns(required, TestRequest::getRequired)
                .returns(optional, TestRequest::getOptional)
                .returns(Optional.empty(), TestRequest::getOmitted)
                .returns(OptionalInt.empty(), TestRequest::getOmittedInt)
                .returns(OptionalLong.empty(), TestRequest::getOmittedLong)
                .returns(OptionalDouble.empty(), TestRequest::getOmittedDouble)
                .returns(DEFAULT, TestRequest::getDefault)
                .returns(data, TestRequest::__data__)
                .returns(auth, TestRequest::__auth__)
                .returns(someBean, TestRequest::someBean);
        assertThat(request.uuid()).isNotNull().isEqualTo(request.uuid());
        assertThat(request.uuidThatChangesEachTime()).isNotNull().isNotEqualTo(request.uuidThatChangesEachTime());
        assertThat(request.string(1)).isNotNull().startsWith("1_").isEqualTo(request.string(1));
        assertThat(request.string(2)).isNotNull().startsWith("2_").isEqualTo(request.string(2));
        assertThat(request.string(1)).isNotEqualTo(request.string(2));
    }

}
