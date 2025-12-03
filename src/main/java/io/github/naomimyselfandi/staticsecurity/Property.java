package io.github.naomimyselfandi.staticsecurity;

import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;

public interface Property {

    String name();

    TypeDescriptor type();

    Method method();

    boolean required();

}
