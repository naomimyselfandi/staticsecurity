package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
final class MergedClearanceResolver implements HandlerMethodArgumentResolver {

    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    final StaticSecurityService staticSecurityService;
    final Supplier<HandlerMethodArgumentResolver> delegateSupplier;

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final HandlerMethodArgumentResolver delegate = delegateSupplier.get();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MergedClearance.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) throws Exception {
        var definition = Objects.requireNonNull(parameter.getParameterAnnotation(MergedClearance.class)).value();
        var constructor = definition.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        var arguments = Arrays
                .stream(constructor.getParameters())
                .map(MethodParameter::forParameter)
                .peek(param -> param.initParameterNameDiscovery(DISCOVERER))
                .map(param -> {
                    try {
                        return getDelegate().resolveArgument(param, mavContainer, webRequest, binderFactory);
                    } catch (Exception e) {
                        var fmt = "Failed resolving parameter %d for %s.";
                        var msg = fmt.formatted(param.getParameterIndex(), param.getDeclaringClass().getName());
                        throw new RuntimeException(msg, e);
                    }
                }).toArray();
        var source = constructor.newInstance(arguments);
        var target = parameter.getParameterType().asSubclass(Clearance.class);
        return staticSecurityService.create(source, target).require();
    }

}
