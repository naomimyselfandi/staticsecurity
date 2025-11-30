package io.github.naomimyselfandi.staticsecurity.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeanCacheTest {

    private interface Something {}

    @Mock
    private Something something;

    @Mock
    private ObjectProvider<Something> objectProvider;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private BeanCache fixture;

    @BeforeEach
    void setup() {
        when(applicationContext.getBeanProvider(any(ResolvableType.class))).then(invocation -> {
            assertThat(invocation.<ResolvableType>getArgument(0).toClass()).isEqualTo(Something.class);
            return objectProvider;
        });
    }

    @Test
    void calculate() {
        when(objectProvider.getObject()).thenReturn(something);
        assertThat(fixture.calculate(ResolvableType.forType(Something.class))).isEqualTo(something);
    }

    @ParameterizedTest
    @ValueSource(classes = {List.class, Collection.class, Iterable.class})
    void calculate_List(Class<?> rawType) {
        when(objectProvider.stream()).then(invocation -> Stream.of(something));
        var type = ResolvableType.forClassWithGenerics(rawType, ResolvableType.forType(Something.class));
        assertThat(fixture.calculate(type)).isEqualTo(List.of(something));
    }

    @Test
    void calculate_Set() {
        when(objectProvider.stream()).then(invocation -> Stream.of(something));
        var type = ResolvableType.forClassWithGenerics(Set.class, ResolvableType.forType(Something.class));
        assertThat(fixture.calculate(type)).isEqualTo(Set.of(something));
    }

}
