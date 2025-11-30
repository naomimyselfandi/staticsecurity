package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;

record DataSourceKey(Class<?> source, Class<? extends Clearance> type) {}
