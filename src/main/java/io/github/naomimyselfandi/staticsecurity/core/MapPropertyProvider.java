package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AbstractProvider;
import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
class MapPropertyProvider extends AbstractProvider<Map<?, ?>> {

    MapPropertyProvider(ConversionService conversionService) {
        super(conversionService);
    }

    @Override
    protected @Nullable Object extractImpl(Map<?, ?> source, Method property) {
        return source.get(MethodInfo.getName(property));
    }

}
