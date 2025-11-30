package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.Authentication;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

class ClearanceInvocationHandler implements InvocationHandler {

    static final Map<Class<?>, Object> DEFAULTS = Map.of(
            Optional.class, Optional.empty(),
            OptionalInt.class, OptionalInt.empty(),
            OptionalLong.class, OptionalLong.empty(),
            OptionalDouble.class, OptionalDouble.empty()
    );

    private record Pair(Method method, List<Object> args) {}

    private final Map<Object, Object> cache = new HashMap<>();

    final Class<?> type;
    final Authentication auth;
    final Map<String, Object> data;
    final Cache<ResolvableType, Object> beanCache;

    ClearanceInvocationHandler(
            Class<?> type,
            Authentication auth,
            Map<String, Object> data,
            Cache<ResolvableType, Object> beanCache
    ) {
        this.type = type;
        this.data = Map.copyOf(data);
        this.auth = auth;
        this.beanCache = beanCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var role = MethodRole.of(method);
        return switch (role) {
            case EQUALS -> proxy == args[0];
            case HASH_CODE -> System.identityHashCode(proxy);
            case TO_STRING -> invokeToString();
            case REQUIRED, OPTIONAL -> invokeProperty(proxy, method, args);
            case AUTH -> auth;
            case DATA -> data;
            case DIRECT_HELPER -> invokeDefault(proxy, method, args);
            case CACHED_HELPER -> invokeCached(proxy, method, args);
            case SPRING_HELPER -> beanCache.get(MethodInfo.getResolvableType(method));
        };
    }

    private String invokeToString() {
        var args = data.entrySet().stream().map(Object::toString).sorted().collect(Collectors.joining(", "));
        return "%s(%s)".formatted(type.getSimpleName(), args);
    }

    private Object invokeProperty(Object proxy, Method method, Object[] args) throws Throwable {
        var value = data.get(MethodInfo.getName(method));
        return value != null ? value : invokeDefault(proxy, method, args);
    }

    private Object invokeCached(Object proxy, Method method, Object[] args) throws Throwable {
        var key = method.getParameterCount() == 0 ? method : new Pair(method, Arrays.asList(args.clone()));
        var value = cache.get(key);
        if (value == null) {
            value = invokeDefault(proxy, method, args);
            cache.put(key, value);
        }
        return value;
    }

    private Object invokeDefault(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        } else {
            return DEFAULTS.get(method.getReturnType());
        }
    }

}
