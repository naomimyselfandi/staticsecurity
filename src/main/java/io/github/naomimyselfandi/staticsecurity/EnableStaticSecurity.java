package io.github.naomimyselfandi.staticsecurity;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the static security library. All library features are enabled if their
 * dependencies are available, and no further configuration is required.
 *
 * <p>This annotation is not required if Spring Boot autoconfiguration is in
 * use.</p>
 */
@Target(ElementType.TYPE)
@Import(StaticSecurity.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableStaticSecurity {}
