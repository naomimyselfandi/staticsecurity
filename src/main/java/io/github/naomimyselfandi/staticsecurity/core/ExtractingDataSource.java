package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

record ExtractingDataSource<S>(PropertyProvider<S> provider, List<Method> properties) implements DataSource<S> {

    @Override
    public Result getData(S source) {
        var result = new HashMap<String, Object>(properties.size());
        for (var property : properties) {
            var extracted = provider.extract(source, property);
            if (extracted != null) {
                result.put(MethodInfo.getName(property), extracted);
            } else if (MethodRole.of(property) == MethodRole.REQUIRED) {
                var message = "Required property '%s' is missing or invalid.".formatted(MethodInfo.getName(property));
                return new Failure(message);
            }
        }
        return () -> result;
    }

}
