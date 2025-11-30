package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

enum MethodRole {

    EQUALS,

    HASH_CODE,

    TO_STRING,

    REQUIRED,

    OPTIONAL,

    AUTH,

    DATA,

    DIRECT_HELPER,

    CACHED_HELPER,

    SPRING_HELPER;

    private static final Map<Method, MethodRole> ROLES = new ConcurrentHashMap<>();

    static MethodRole of(Method method) {
        return ROLES.computeIfAbsent(method, MethodRole::getRole0);
    }

    private static MethodRole getRole0(Method method) {
        if (MethodInfo.isProperty(method)) {
            return method.isDefault() || ClearanceInvocationHandler.DEFAULTS.containsKey(method.getReturnType()) ? OPTIONAL : REQUIRED;
        } else if (ReflectionUtils.isEqualsMethod(method)) {
            return EQUALS;
        } else if (ReflectionUtils.isHashCodeMethod(method)) {
            return HASH_CODE;
        } else if (ReflectionUtils.isToStringMethod(method)) {
            return TO_STRING;
        } else if (method.getName().equals("__auth__") && method.getParameterCount() == 0) {
            return AUTH;
        } else if (method.getName().equals("__data__") && method.getParameterCount() == 0) {
            return DATA;
        } else if (method.isAnnotationPresent(Clearance.Helper.class)) {
            return switch (method.getAnnotation(Clearance.Helper.class).value()) {
                case DIRECT -> validateDefaultHelper(method, DIRECT_HELPER);
                case CACHED -> validateDefaultHelper(method, CACHED_HELPER);
                case SPRING -> validateSpringHelper(method);
            };
        } else {
            throw new IllegalStateException("Misconfigured clearance method %s.".formatted(method));
        }
    }

    private static MethodRole validateDefaultHelper(Method method, MethodRole result) {
        if (method.isDefault()) {
            return result;
        } else {
            throw new IllegalStateException("Helper method %s should have a default implementation.".formatted(method));
        }
    }

    private static MethodRole validateSpringHelper(Method method) {
        if (method.isDefault() || method.getParameterCount() > 0 || method.getReturnType() == void.class) {
            throw new IllegalStateException("Misconfigured Spring helper method %s.".formatted(method));
        } else {
            return SPRING_HELPER;
        }
    }

}
