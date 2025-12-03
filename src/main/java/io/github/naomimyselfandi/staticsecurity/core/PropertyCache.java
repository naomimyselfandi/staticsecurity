package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
class PropertyCache extends Cache<Class<?>, List<Property>> {

    @Override
    List<Property> calculate(Class<?> input) {
        return Arrays
                .stream(input.getMethods())
                .filter(ClearanceInvocationHandler::isProperty)
                .sorted(Comparator.comparing(Method::getName))
                .map(PropertyCache::create)
                .toList();
    }

    private static Property create(Method method) {
        var name = ClearanceInvocationHandler.name(method);
        var resolvableType = ResolvableType.forMethodReturnType(Objects.requireNonNull(method));
        var type = new TypeDescriptor(resolvableType, null, null);
        var hasImplicitDefault = ClearanceInvocationHandler.DEFAULTS.containsKey(method.getReturnType());
        var hasExplicitDefault = method.isDefault();
        return new PropertyImpl(name, type, method, !(hasImplicitDefault || hasExplicitDefault));
    }

}
