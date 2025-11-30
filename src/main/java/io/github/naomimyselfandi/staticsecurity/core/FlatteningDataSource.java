package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;

import java.lang.reflect.Method;
import java.util.Map;

record FlatteningDataSource<S>(PropertyProvider<S> provider, Method property) implements DataSource<S> {

    @Override
    public Result getData(S source) {
        var name = MethodInfo.getName(property);
        var flattened = provider.flatten(source, property);
        if (flattened != null) {
            return () -> Map.of(name, flattened);
        } else {
            return new Failure("Required property '%s' is missing or invalid.".formatted(name));
        }
    }

}
