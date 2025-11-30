package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.ClearanceSourceException;

import java.util.Map;

interface DataSource<S> {

    @FunctionalInterface
    interface Result {
        Map<String, Object> get();
    }

    Result getData(S source);

    record Failure(String reason) implements Result {

        @Override
        public Map<String, Object> get() {
            throw new ClearanceSourceException(reason);
        }

    }

}
