package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.PendingClearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    private UriComponentsBuilder uriBuilder;

    @Mock
    private ConversionService conversionService;

    @Mock
    private HandlerMethodArgumentResolver resolver;

    @Mock
    private UriComponentsContributor contributor;

    @Mock
    private StaticSecurityService staticSecurityService;

    @Mock
    private Supplier<HandlerMethodArgumentResolver> resolverSupplier;

    @Mock
    private Supplier<UriComponentsContributor> contributorSupplier;

    private MergedClearanceResolver fixture;

    @BeforeEach
    void setup() {
        fixture = new MergedClearanceResolver(staticSecurityService, resolverSupplier, contributorSupplier);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void supportsParameter(boolean canCreate) {
        when(parameter.getParameterAnnotation(MergedClearance.class)).thenReturn(mergedClearance);
        doReturn(TestClearance.class).when(parameter).getParameterType();
        doReturn(Helper.class).when(mergedClearance).value();
        when(staticSecurityService.canCreate(Helper.class, TestClearance.class)).thenReturn(canCreate);
        assertThat(fixture.supportsParameter(parameter)).isEqualTo(canCreate);
    }

    @Test
    void supportsParameter_WhenTheParameterIsNotAnnotated_ThenFalse() {
        when(parameter.getParameterAnnotation(MergedClearance.class)).thenReturn(null);
        assertThat(fixture.supportsParameter(parameter)).isFalse();
    }

    @Test
    void resolveArgument() throws Exception {
        var constructor = Helper.class.getDeclaredConstructors()[0];
        var fooParam = new MethodParameter(constructor, 0);
        var barParam = new MethodParameter(constructor, 1);
        var foo = new Object();
        var bar = new Object();
        when(resolverSupplier.get()).thenReturn(resolver);
        when(resolver.resolveArgument(fooParam, mavContainer, webRequest, binderFactory)).then(i -> {
            var parameter = i.<MethodParameter>getArgument(0);
            assertThat(parameter.getParameterName()).isEqualTo("foo");
            assertThat(parameter.getExecutable()).isEqualTo(constructor);
            assertThat(parameter.getParameterIndex()).isZero();
            return foo;
        });
        when(resolver.resolveArgument(barParam, mavContainer, webRequest, binderFactory)).then(i -> {
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
        when(resolverSupplier.get()).thenReturn(resolver);
        if (i == 0) {
            when(resolver.resolveArgument(fooParam, mavContainer, webRequest, binderFactory)).thenThrow(e);
        } else {
            when(resolver.resolveArgument(fooParam, mavContainer, webRequest, binderFactory)).thenReturn(new Object());
            when(resolver.resolveArgument(barParam, mavContainer, webRequest, binderFactory)).thenThrow(e);
        }
        when(parameter.getParameterAnnotation(MergedClearance.class)).thenReturn(mergedClearance);
        doReturn(Helper.class).when(mergedClearance).value();
        assertThatThrownBy(() -> fixture.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed resolving parameter %d for %s.", i, Helper.class.getName())
                .hasCause(e);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,true
            true,false
            false,true
            false,false
            """)
    void contributeMethodArgument(boolean fooIsSet, boolean barIsSet) {
        var constructor = Helper.class.getDeclaredConstructors()[0];
        var fooParam = new MethodParameter(constructor, 0);
        var barParam = new MethodParameter(constructor, 1);
        var foo = fooIsSet ? new Object() : null;
        var bar = barIsSet ? new Object() : null;
        var data = new HashMap<String, Object>();
        if (fooIsSet) {
            data.put("foo", foo);
        }
        if (barIsSet) {
            data.put("bar", bar);
        }
        when(clearance.__data__()).thenReturn(data);
        var uriVariables = Map.of(UUID.randomUUID().toString(), new Object());
        when(contributorSupplier.get()).thenReturn(contributor);
        when(parameter.getParameterAnnotation(MergedClearance.class)).thenReturn(mergedClearance);
        doReturn(Helper.class).when(mergedClearance).value();
        fixture.contributeMethodArgument(parameter, clearance, uriBuilder, uriVariables, conversionService);
        if (fooIsSet) {
            verify(contributor).contributeMethodArgument(fooParam, foo, uriBuilder, uriVariables, conversionService);
        }
        if (barIsSet) {
            verify(contributor).contributeMethodArgument(barParam, bar, uriBuilder, uriVariables, conversionService);
        }
        verifyNoMoreInteractions(contributor);
    }

}
