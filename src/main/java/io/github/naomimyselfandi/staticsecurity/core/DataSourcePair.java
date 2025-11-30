package io.github.naomimyselfandi.staticsecurity.core;

record DataSourcePair<S>(DataSource<S> primary, DataSource<S> secondary) implements DataSource<S> {

    @Override
    public Result getData(S source) {
        var primaryResult = primary.getData(source);
        if (primaryResult.getClass() == Failure.class) {
            var secondaryResult = secondary.getData(source);
            if (secondaryResult.getClass() != Failure.class) {
                return secondaryResult;
            }
        }
        return primaryResult;
    }

}
