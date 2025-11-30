package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
class AccessPolicyCache extends Cache<Class<?>, List<? extends AccessPolicy<?>>> {

    private final List<AccessPolicy<?>> accessPolicies;

    AccessPolicyCache(List<? extends AccessPolicy<?>> accessPolicies) {
        this.accessPolicies = accessPolicies
                .stream()
                .sorted(Comparator.comparing(it -> depth(it.getClearanceType())))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    List<AccessPolicy<?>> calculate(Class<?> input) {
        return accessPolicies
                .stream()
                .filter(it -> it.getClearanceType().isAssignableFrom(input))
                .toList();
    }

    private static int depth(Class<?> type) {
        return 1 + Arrays.stream(type.getInterfaces()).mapToInt(AccessPolicyCache::depth).max().orElse(0);
    }

}
