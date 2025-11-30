package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.PendingClearance;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

record PendingClearanceImpl<C extends Clearance>(
        Class<C> type,
        Map<String, Object> data,
        List<? extends AccessPolicy<? super C>> policies,
        Cache<ResolvableType, Object> beanCache
) implements PendingClearance<C> {

    private static final ClassLoader CLASS_LOADER = PendingClearanceImpl.class.getClassLoader();

    @Override
    public @Nullable C get(boolean nullable) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var handler = new ClearanceInvocationHandler(type, auth, data, beanCache);
        @SuppressWarnings("unchecked")
        var clearance = (C) Proxy.newProxyInstance(CLASS_LOADER, new Class<?>[]{type}, handler);
        for (var accessPolicy : policies) {
            var denial = accessPolicy.check(clearance);
            if (denial != null) {
                if (nullable) {
                    return null;
                } else {
                    throw denial.get();
                }
            }
        }
        return clearance;
    }

}
