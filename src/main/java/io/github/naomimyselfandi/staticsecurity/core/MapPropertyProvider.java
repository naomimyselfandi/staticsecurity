package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AbstractProvider;
import io.github.naomimyselfandi.staticsecurity.Property;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class MapPropertyProvider extends AbstractProvider<Map<?, ?>> {

    MapPropertyProvider(ConversionService conversionService) {
        super(conversionService);
    }

    @Override
    protected @Nullable Object extractImpl(Map<?, ?> source, Property property) {
        return source.get(property.name());
    }

}
