package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.PendingClearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MergedClearanceResolverTest {

    private interface TestClearance extends Clearance {}

    private record Helper(Object foo, Object bar) {}

    @Mock
    private TestClearance clearance;

    @Mock
    private PendingClearance<TestClearance> pendingClearance;

    @Mock
    private MergedClearance mergedClearance;

    @Mock
    private MethodParameter parameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private HandlerMethodArgumentResolver delegate;

    @Mock
    private StaticSecurityService staticSecurityService;

    @Mock
    private Supplier<HandlerMethodArgumentResolver> delegateSupplier;

    @InjectMocks
    private MergedClearanceResolver fixture;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void supportsParameter(boolean annotated) {
        when(parameter.hasParameterAnnotation(MergedClearance.class)).thenReturn(annotated);
        assertThat(fixture.supportsParameter(parameter)).isEqualTo(annotated);
    }

    @Test
    void resolveArgument() throws Exception {
        var constructor = Helper.class.getDeclaredConstructors()[0];
        var fooParam = new MethodParameter(constructor, 0);
        var barParam = new MethodParameter(constructor, 1);
        var foo = new Object();
        var bar = new Object();
        when(delegateSupplier.get()).thenReturn(delegate);
        when(delegate.resolveArgument(fooParam, mavContainer, webRequest, binderFactory)).then(i -> {
            var parameter = i.<MethodParameter>getArgument(0);
            assertThat(parameter.getParameterName()).isEqualTo("foo");
            assertThat(parameter.getExecutable()).isEqualTo(constructor);
            assertThat(parameter.getParameterIndex()).isZero();
            return foo;
        });
        when(delegate.resolveArgument(barParam, mavContainer, webRequest, binderFactory)).then(i -> {
            var parameter = i.<MethodParameter>getArgument(0);
            assertThat(parameter.getParameterName()).isEqualTo("bar");
            assertThat(parameter.getExecutable()).isEqualTo(constructor);
            assertThat(parameter.getParameterIndex()).isOne();
            return bar;
        });
        when(parameter.getParameterAnnotation(MergedClearance.class)).thenReturn(mergedClearance);
        doReturn(TestClearance.class).when(parameter).getParameterType();
        doReturn(Helper.class).when(mergedClearance).value();
        when(staticSecurityService.create(new Helper(foo, bar), TestClearance.class)).thenReturn(pendingClearance);
        when(pendingClearance.require()).thenReturn(clearance);
        assertThat(fixture.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo(clearance);
    }

    @RepeatedTest(2)
    void resolveArgument_WhenResolutionFails_ThenThrows(RepetitionInfo repetitionInfo) throws Exception {
        var e = new Exception();
        var i = repetitionInfo.getCurrentRepetition() - 1;
        var constructor = Helper.class.getDeclaredConstructors()[0];
        var fooParam = new MethodParameter(constructor, 0);
        var barParam = new MethodParameter(constructor, 1);
        when(delegateSupplier.get()).thenReturn(delegate);
        if (i == 0) {
            when(delegate.resolveArgument(fooParam, mavContainer, webRequest, binderFactory)).thenThrow(e);
        } else {
            when(delegate.resolveArgument(fooParam, mavContainer, webRequest, binderFactory)).thenReturn(new Object());
            when(delegate.resolveArgument(barParam, mavContainer, webRequest, binderFactory)).thenThrow(e);
        }
        when(parameter.getParameterAnnotation(MergedClearance.class)).thenReturn(mergedClearance);
        doReturn(Helper.class).when(mergedClearance).value();
        assertThatThrownBy(() -> fixture.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed resolving parameter %d for %s.", i, Helper.class.getName())
                .hasCause(e);
    }

}
