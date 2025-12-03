package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;

import java.util.Map;

record FlatteningDataSource<S>(PropertyProvider<S> provider, Property property) implements DataSource<S> {

    @Override
    public Result getData(S source) {
        var name = property.name();
        var flattened = provider.flatten(source, property);
        if (flattened != null) {
            return () -> Map.of(name, flattened);
        } else {
            return new Failure("Required property '%s' is missing or invalid.".formatted(name));
        }
    }

}
