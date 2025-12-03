package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticSecurityConfigurationTest {

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private StaticSecurityService securityService;

    @Mock
    private ConfigurableConversionService conversionService;

    @Mock
    private Cache<Class<?>, List<Property>> propertyCache;

    @Mock
    private ObjectProvider<ConfigurableConversionService> conversionServices;

    @InjectMocks
    private StaticSecurityConfiguration fixture;

    @Test
    void afterSingletonsInstantiated() {
        when(conversionServices.iterator()).then(invocation -> List.of(conversionService).iterator());
        fixture.afterSingletonsInstantiated();
        verify(conversionService).addConverter(new ClearanceConverter(securityService));
        verify(conversionService).addConverter(new ClearanceReverseConverter(propertyCache, conversionService));
    }

    @Test
    void staticSecurityPostProcessor() {
        var beanFactory = new DefaultListableBeanFactory(); // not mockable
        var delegate = beanFactory.getAutowireCandidateResolver();
        StaticSecurityConfiguration.staticSecurityPostProcessor().postProcessBeanFactory(beanFactory);
        var expected = new AutowireCandidateResolverImpl(delegate, beanFactory);
        assertThat(beanFactory.getAutowireCandidateResolver()).isEqualTo(expected);
    }

    @Test
    void staticSecurityPostProcessor_WhenTheBeanFactoryIsAnotherType_ThenDoesNothing() {
        StaticSecurityConfiguration.staticSecurityPostProcessor().postProcessBeanFactory(beanFactory);
        verifyNoInteractions(beanFactory);
    }

}
