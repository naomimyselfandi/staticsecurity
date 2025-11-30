package io.github.naomimyselfandi.staticsecurity.core;

import org.springframework.util.ConcurrentLruCache;

abstract class Cache<T, R> {

    private final ConcurrentLruCache<T, R> cache = new ConcurrentLruCache<>(256, this::calculate);

    R get(T input) {
        return cache.get(input);
    }

    abstract R calculate(T input);

}
