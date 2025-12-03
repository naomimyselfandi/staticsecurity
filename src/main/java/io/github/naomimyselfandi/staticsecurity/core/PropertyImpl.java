package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Property;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;

record PropertyImpl(String name, TypeDescriptor type, Method method, boolean required) implements Property {}
