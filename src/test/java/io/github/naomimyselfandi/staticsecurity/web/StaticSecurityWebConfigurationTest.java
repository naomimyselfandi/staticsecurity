package io.github.naomimyselfandi.staticsecurity.web;

import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticSecurityWebConfigurationTest {

    @Mock
    private HandlerMethodArgumentResolver foo, bar, baz;

    @Mock
    private RequestMappingHandlerAdapter adapter;

    @Mock
    private StaticSecurityService staticSecurityService;

    @Mock
    private ObjectProvider<RequestMappingHandlerAdapter> adapterProvider;

    @InjectMocks
    private StaticSecurityWebConfiguration fixture;

    @Test
    void addArgumentResolvers() {
        var list = new ArrayList<HandlerMethodArgumentResolver>();
        list.add(foo);
        when(adapterProvider.getObject()).thenReturn(adapter);
        when(adapter.getArgumentResolvers()).thenReturn(List.of(foo, bar, baz));
        fixture.addArgumentResolvers(list);
        assertThat(list).hasSize(2).first().isEqualTo(foo);
        assertThat(list).last()
                .asInstanceOf(InstanceOfAssertFactories.type(MergedClearanceResolver.class))
                .returns(staticSecurityService, it -> it.staticSecurityService)
                .extracting(it -> it.delegateSupplier.get())
                .asInstanceOf(InstanceOfAssertFactories.type(HandlerMethodArgumentResolverComposite.class))
                .extracting(HandlerMethodArgumentResolverComposite::getResolvers)
                .isEqualTo(List.of(foo, bar, baz));
    }

}
