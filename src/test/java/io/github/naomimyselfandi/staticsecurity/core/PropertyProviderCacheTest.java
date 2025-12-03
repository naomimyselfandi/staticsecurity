package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyProviderCacheTest {

    private interface Foo {}
    private interface Bar extends Foo {}
    private static class FooImpl implements Foo {}
    private static class SubImpl extends FooImpl {}
    private static class BarImpl extends SubImpl implements Bar {}
    private interface SomethingElse {}

    @Mock
    private Cache<Class<?>, List<Property>> propertyCache;

    @Mock
    private Property property;

    @Mock
    private PropertyProvider<Foo> foo;

    @Mock
    private PropertyProvider<Bar> bar;

    @Mock
    private PropertyProvider<FooImpl> fooImpl;

    @Mock
    private PropertyProvider<SubImpl> subImpl;

    @Mock
    private PropertyProvider<BarImpl> barImpl;

    @Mock
    private ConversionService conversionService;

    private PropertyProviderCache fixture;

    @BeforeEach
    void setup() {
        when(foo.getSourceType()).thenReturn(Foo.class);
        when(bar.getSourceType()).thenReturn(Bar.class);
        when(fooImpl.getSourceType()).thenReturn(FooImpl.class);
        when(subImpl.getSourceType()).thenReturn(SubImpl.class);
        when(barImpl.getSourceType()).thenReturn(BarImpl.class);
        var extractors = new ArrayList<PropertyProvider<?>>();
        extractors.add(foo);
        extractors.add(bar);
        extractors.add(fooImpl);
        extractors.add(subImpl);
        extractors.add(barImpl);
        Collections.shuffle(extractors);
        fixture = new PropertyProviderCache(propertyCache, extractors, conversionService);
    }

    @RepeatedTest(5)
    void calculate() {
        assertThat(fixture.calculate(Foo.class)).isEqualTo(foo);
        assertThat(fixture.calculate(Bar.class)).isEqualTo(bar);
        assertThat(fixture.calculate(FooImpl.class)).isEqualTo(fooImpl);
        assertThat(fixture.calculate(SubImpl.class)).isEqualTo(subImpl);
        assertThat(fixture.calculate(BarImpl.class)).isEqualTo(barImpl);
    }

    @Test
    void calculate_WhenNoExtractorIsConfigured_ThenUsesAReflectiveExtractor() {
        when(property.name()).thenReturn(UUID.randomUUID().toString());
        when(propertyCache.calculate(SomethingElse.class)).thenReturn(List.of(property));
        assertThat(fixture.calculate(SomethingElse.class))
                .asInstanceOf(InstanceOfAssertFactories.type(ReflectivePropertyProvider.class))
                .returns(SomethingElse.class, PropertyProvider::getSourceType)
                .returns(Map.of(property.name(), property), it -> it.properties)
                .returns(conversionService, it -> it.conversionService)
                .returns(fixture, it -> it.propertyProviderCache);
    }

}
