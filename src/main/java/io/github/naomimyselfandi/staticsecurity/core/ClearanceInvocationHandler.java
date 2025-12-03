package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ClearanceInvocationHandler implements InvocationHandler {

    static final Map<Class<?>, Object> DEFAULTS = Map.of(
            Optional.class, Optional.empty(),
            OptionalInt.class, OptionalInt.empty(),
            OptionalLong.class, OptionalLong.empty(),
            OptionalDouble.class, OptionalDouble.empty()
    );
    private static final Pattern RESERVED = Pattern.compile("__.*__");
    private static final Pattern NORMALIZER = Pattern.compile("(?:get|is)([A-Z])(.*)");
    private static final Set<String> NOT_PROPERTIES = Set.of("hashCode", "toString", "getClass", "clone");

    static String name(Method method) {
        var name = method.getName();
        var matcher = NORMALIZER.matcher(name);
        return matcher.matches() ? (matcher.group(1).toLowerCase() + matcher.group(2)) : name;
    }

    static boolean isProperty(Method method) {
        return (method.getParameterCount() == 0)
                && (method.getReturnType() != void.class)
                && !NOT_PROPERTIES.contains(method.getName())
                && !RESERVED.matcher(method.getName()).matches()
                && !method.isAnnotationPresent(Clearance.Helper.class);
    }

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
        if (ReflectionUtils.isEqualsMethod(method)) {
            return proxy == args[0];
        } else if (ReflectionUtils.isHashCodeMethod(method)) {
            return System.identityHashCode(proxy);
        } else if (ReflectionUtils.isToStringMethod(method)) {
            return invokeToString();
        } else if (isProperty(method)) {
            return invokeProperty(proxy, method, args);
        } else if ("__auth__".equals(method.getName()) && method.getParameterCount() == 0) {
            return auth;
        } else if ("__data__".equals(method.getName()) && method.getParameterCount() == 0) {
            return data;
        } else if (method.isAnnotationPresent(Clearance.Helper.class)) {
            return switch (method.getAnnotation(Clearance.Helper.class).value()) {
                case DIRECT -> InvocationHandler.invokeDefault(proxy, method, args);
                case CACHED -> invokeCachedHelper(proxy, method, args);
                case SPRING -> beanCache.get(ResolvableType.forMethodReturnType(method));
            };
        } else {
            throw new IllegalStateException("Unsupported method %s.".formatted(method));
        }
    }

    private String invokeToString() {
        var args = data.entrySet().stream().map(Object::toString).sorted().collect(Collectors.joining(", "));
        return "%s(%s)".formatted(type.getSimpleName(), args);
    }

    private Object invokeProperty(Object proxy, Method method, Object[] args) throws Throwable {
        var value = data.get(name(method));
        if (value != null) {
            return value;
        } else if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        } else {
            return DEFAULTS.get(method.getReturnType());
        }
    }

    private Object invokeCachedHelper(Object proxy, Method method, Object[] args) throws Throwable {
        var key = method.getParameterCount() == 0 ? method : new Pair(method, Arrays.asList(args.clone()));
        var value = cache.get(key);
        if (value == null) {
            value = InvocationHandler.invokeDefault(proxy, method, args);
            cache.put(key, value);
        }
        return value;
    }

}
