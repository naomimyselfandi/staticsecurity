package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;

import java.util.HashMap;
import java.util.List;

record ExtractingDataSource<S>(PropertyProvider<S> provider, List<Property> properties) implements DataSource<S> {

    @Override
    public Result getData(S source) {
        var result = new HashMap<String, Object>(properties.size());
        for (var property : properties) {
            var extracted = provider.extract(source, property);
            if (extracted != null) {
                result.put(property.name(), extracted);
            } else if (property.required()) {
                var message = "Required property '%s' is missing or invalid.".formatted(property.name());
                return new Failure(message);
            }
        }
        return () -> result;
    }

}
