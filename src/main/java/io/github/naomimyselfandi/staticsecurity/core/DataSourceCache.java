package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class DataSourceCache extends Cache<DataSourceKey, Optional<? extends DataSource<?>>> {

    private final Cache<Class<?>, List<Property>> propertyCache;
    private final Cache<Class<?>, PropertyProvider<?>> propertyProviderCache;

    @Override
    Optional<? extends DataSource<?>> calculate(DataSourceKey input) {
        return calculate(input.source(), input.type());
    }

    private <S> Optional<DataSource<S>> calculate(Class<S> source, Class<? extends Clearance> type) {
        @SuppressWarnings("unchecked")
        var provider = (PropertyProvider<S>) propertyProviderCache.get(source);
        var result = Stream.<DataSource<S>>builder();
        var properties = propertyCache.get(type);
        var requiredProperties = properties.stream().filter(Property::required).toList();
        if (requiredProperties.stream().allMatch(provider::canExtract)) {
            result.add(new ExtractingDataSource<>(provider, properties));
        }
        if (requiredProperties.size() == 1 && provider.canFlatten(requiredProperties.get(0))) {
            result.add(new FlatteningDataSource<>(provider, requiredProperties.get(0)));
        }
        return result.build().reduce(DataSourcePair::new);
    }

}
