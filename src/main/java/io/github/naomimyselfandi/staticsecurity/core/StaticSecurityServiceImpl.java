package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class StaticSecurityServiceImpl implements StaticSecurityService {

    private final Cache<Class<?>, List<? extends AccessPolicy<?>>> accessPolicyCache;
    private final Cache<ResolvableType, Object> beanCache;
    private final Cache<DataSourceKey, Optional<? extends DataSource<?>>> dataSourceCache;

    @Override
    public boolean canCreate(Class<?> source, Class<?> type) {
        return Clearance.class.isAssignableFrom(type)
                && type.isInterface()
                && dataSourceCache.get(new DataSourceKey(source, type.asSubclass(Clearance.class))).isPresent();
    }

    @Override
    public <S, C extends Clearance> PendingClearance<C> create(S source, Class<C> type) {
        @SuppressWarnings("unchecked")
        var dataSource = (DataSource<S>) dataSourceCache
                .get(new DataSourceKey(source.getClass(), type))
                .orElseThrow(() -> new ClearanceSourceException("Cannot create %s from %s.".formatted(type, source)));
        var data = dataSource.getData(source).get();
        @SuppressWarnings("unchecked")
        var accessPolicies = (List<AccessPolicy<? super C>>) accessPolicyCache.get(type);
        return new PendingClearanceImpl<>(type, data, accessPolicies, beanCache);
    }

}
