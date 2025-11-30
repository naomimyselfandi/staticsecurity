package io.github.naomimyselfandi.staticsecurity.core;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class BeanCache extends Cache<ResolvableType, Object> {

    final ApplicationContext applicationContext;

    @Override
    Object calculate(ResolvableType resolvableType) {
        var type = resolvableType.toClass();
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            return applicationContext.getBeanProvider(resolvableType.getGeneric()).stream().toList();
        } else if (type == Set.class) {
            return applicationContext
                    .getBeanProvider(resolvableType.getGeneric())
                    .stream()
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            return applicationContext.getBeanProvider(resolvableType).getObject();
        }
    }

}
