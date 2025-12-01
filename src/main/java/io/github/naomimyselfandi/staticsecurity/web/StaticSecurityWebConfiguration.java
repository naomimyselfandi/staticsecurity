package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.CompositeUriComponentsContributor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
class StaticSecurityWebConfiguration implements WebMvcConfigurer {

    private final StaticSecurityService staticSecurityService;
    private final ObjectProvider<RequestMappingHandlerAdapter> adapterProvider;

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final List<HandlerMethodArgumentResolver> resolvers = adapterProvider.getObject().getArgumentResolvers();

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MergedClearanceResolver(staticSecurityService, () -> {
            var composite = new HandlerMethodArgumentResolverComposite();
            composite.addResolvers(getResolvers());
            return composite;
        }, () -> new CompositeUriComponentsContributor(getResolvers())));
    }

}
