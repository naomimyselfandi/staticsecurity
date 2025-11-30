package io.github.naomimyselfandi.staticsecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Unwrap the annotated method's return value during property extraction. While
 * extracting a property value from an object with unwrapped methods, the value
 * may also be extracted from those methods' return values.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unwrap {}
