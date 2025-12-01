package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
final class MergedClearanceResolver implements HandlerMethodArgumentResolver, UriComponentsContributor {

    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    final StaticSecurityService staticSecurityService;
    final Supplier<HandlerMethodArgumentResolver> resolverSupplier;
    final Supplier<UriComponentsContributor> contributorSupplier;

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final HandlerMethodArgumentResolver resolverDelegate = resolverSupplier.get();

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final UriComponentsContributor contributorDelegate = contributorSupplier.get();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Optional
                .ofNullable(parameter.getParameterAnnotation(MergedClearance.class))
                .map(MergedClearance::value)
                .filter(it -> staticSecurityService.canCreate(it, parameter.getParameterType()))
                .isPresent();
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
                        return getResolverDelegate().resolveArgument(param, mavContainer, webRequest, binderFactory);
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

    @Override
    public void contributeMethodArgument(
            MethodParameter parameter,
            Object value,
            UriComponentsBuilder builder,
            Map<String, Object> uriVariables,
            ConversionService conversionService
    ) {
        var data = ((Clearance) value).__data__();
        var delegate = getContributorDelegate();
        var definition = Objects.requireNonNull(parameter.getParameterAnnotation(MergedClearance.class)).value();
        var constructor = definition.getDeclaredConstructors()[0];
        for (var i : constructor.getParameters()) {
            var param = MethodParameter.forParameter(i);
            param.initParameterNameDiscovery(DISCOVERER);
            var datum = data.get(param.getParameterName());
            if (datum != null) {
                delegate.contributeMethodArgument(param, datum, builder, uriVariables, conversionService);
            }
        }
    }

}
