package io.github.naomimyselfandi.staticsecurity;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Unmodifiable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A utility class for getting information about methods.
 */
@UtilityClass
public class MethodInfo {

    private final Pattern NORMALIZER = Pattern.compile("(?:get|is)([A-Z])(.*)");
    private final Pattern RESERVED = Pattern.compile("__.*__|hashCode|toString");

    private final Map<Method, String> NAMES = new ConcurrentHashMap<>();
    private final Map<Method, TypeDescriptor> TYPES = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> PROPERTIES = new ConcurrentHashMap<>();

    /**
     * Get a method's name without any {@code get} or {@code is} prefix.
     * @param method The method.
     * @return The method's name without any prefixes.
     */
    public String getName(Method method) {
        return NAMES.computeIfAbsent(method, MethodInfo::getName0);
    }

    /**
     * Get a method's return type as a Spring {@code TypeDescriptor}.
     * @param method The method.
     * @return A {@code TypeDescriptor} for the method's return type.
     */
    public TypeDescriptor getType(Method method) {
        return TYPES.computeIfAbsent(method, MethodInfo::getType0);
    }

    /**
     * Get a method's return type as a Spring {@code ResolvableType}.
     * @param method The method.
     * @return A {@code ResolvableType} for the method's return type.
     */
    public ResolvableType getResolvableType(Method method) {
        return getType(method).getResolvableType();
    }

    /**
     * Test if a method is a property getter. Specifically, the method must not
     * have parameters, must return a type other than {@code void}, must not be
     * annotated with {@link Clearance.Helper}, and must not have a name which
     * begins and ends with two underscores. Additionally, {@link #hashCode()}
     * and {@link #toString()} are never getters, even though they technically
     * meet those requirements.
     * @param method The method.
     * @return {@code true} iff the method is a property getter.
     */
    public boolean isProperty(Method method) {
        return (method.getParameterCount() == 0)
                && (method.getReturnType() != void.class)
                && !RESERVED.matcher(method.getName()).matches()
                && !method.isAnnotationPresent(Clearance.Helper.class);
    }

    /**
     * Get all the property getters for a type.
     * @param type The type.
     * @return All of that type's property getters.
     */
    public @Unmodifiable List<Method> getProperties(Class<?> type) {
        return PROPERTIES.computeIfAbsent(type, MethodInfo::getProperties0);
    }

    private String getName0(Method method) {
        var name = method.getName();
        var matcher = NORMALIZER.matcher(name);
        return matcher.matches() ? (matcher.group(1).toLowerCase() + matcher.group(2)) : name;
    }

    private TypeDescriptor getType0(Method method) {
        var resolvableType = ResolvableType.forMethodReturnType(Objects.requireNonNull(method));
        return new TypeDescriptor(resolvableType, null, null);
    }

    private @Unmodifiable List<Method> getProperties0(Class<?> type) {
        return Arrays
                .stream(type.getMethods())
                .filter(MethodInfo::isProperty)
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

}
