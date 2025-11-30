package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;

@Configuration
@ComponentScan
@RequiredArgsConstructor
class StaticSecurityConfiguration implements SmartInitializingSingleton {

    private final StaticSecurityService staticSecurityService;
    private final ObjectProvider<ConfigurableConversionService> conversionServices;

    @Override
    public void afterSingletonsInstantiated() {
        for (var conversionService : conversionServices) {
            conversionService.addConverter(new ClearanceConverter(staticSecurityService));
            conversionService.addConverter(new ClearanceReverseConverter(conversionService));
        }
    }

    @Bean
    static BeanFactoryPostProcessor staticSecurityPostProcessor() {
        return beanFactory -> {
            if (beanFactory instanceof DefaultListableBeanFactory factory) {
                var delegate = factory.getAutowireCandidateResolver();
                var resolver = new AutowireCandidateResolverImpl(delegate, beanFactory);
                factory.setAutowireCandidateResolver(resolver);
            }
        };
    }

}
