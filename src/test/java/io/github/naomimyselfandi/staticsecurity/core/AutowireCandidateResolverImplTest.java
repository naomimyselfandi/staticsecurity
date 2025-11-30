package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.ClearanceFactory;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutowireCandidateResolverImplTest {

    private interface Source {}

    private interface Target extends Clearance {}

    @Mock
    private ClearanceFactory<Source, Target> factory;

    @Mock
    private StaticSecurityService staticSecurityService;

    @Mock
    private DependencyDescriptor dependencyDescriptor;

    @Mock
    private AutowireCandidateResolver delegate, clone;

    @Mock
    private BeanFactory beanFactory;

    private AutowireCandidateResolverImpl fixture;

    @BeforeEach
    void setup() {
        fixture = new AutowireCandidateResolverImpl(delegate, beanFactory);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,true
            true,false
            false,true
            false,false
            """)
    void getSuggestedValue(boolean canCreate, boolean required) {
        var type = ResolvableType.forClassWithGenerics(ClearanceFactory.class, Source.class, Target.class);
        when(dependencyDescriptor.getResolvableType()).thenReturn(type);
        when(beanFactory.getBean(StaticSecurityService.class)).thenReturn(staticSecurityService);
        lenient().when(delegate.isRequired(dependencyDescriptor)).thenReturn(required);
        lenient().when(staticSecurityService.canCreate(Source.class, Target.class)).thenReturn(canCreate);
        if (canCreate || required) {
            when(staticSecurityService.createFactory(Source.class, Target.class)).thenReturn(factory);
            assertThat(fixture.getSuggestedValue(dependencyDescriptor)).isEqualTo(factory);
        } else {
            assertThat(fixture.getSuggestedValue(dependencyDescriptor)).isNull();
            verify(staticSecurityService, never()).createFactory(Source.class, Target.class);
        }
    }

    @Test
    void getSuggestedValue_WhenTheDelegateHasAValue_ThenUsesIt() {
        var delegateResult = new Object();
        when(delegate.getSuggestedValue(dependencyDescriptor)).thenReturn(delegateResult);
        assertThat(fixture.getSuggestedValue(dependencyDescriptor)).isEqualTo(delegateResult);
    }

    @Test
    void getSuggestedValue_WhenTheDependencyIsAnUnsupportedType_ThenDoesNothing() {
        interface Something {}
        when(dependencyDescriptor.getResolvableType()).thenReturn(ResolvableType.forType(Something.class));
        assertThat(fixture.getSuggestedValue(dependencyDescriptor)).isNull();
    }

    @Test
    void cloneIfNecessary() {
        when(delegate.cloneIfNecessary()).thenReturn(clone);
        assertThat(fixture.cloneIfNecessary()).isEqualTo(new AutowireCandidateResolverImpl(clone, beanFactory));
    }

}
