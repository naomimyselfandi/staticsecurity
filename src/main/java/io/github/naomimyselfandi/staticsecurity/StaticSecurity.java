package io.github.naomimyselfandi.staticsecurity;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;

record StaticSecurity(ClassLoader classLoader) implements ImportSelector {

    StaticSecurity() {
        this(StaticSecurity.class.getClassLoader());
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        var result = new ArrayList<String>();
        result.add("io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration");
        try {
            classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            result.add("io.github.naomimyselfandi.staticsecurity.jackson.StaticSecurityJacksonConfiguration");
            classLoader.loadClass("org.springframework.web.method.support.HandlerMethodArgumentResolver");
            result.add("io.github.naomimyselfandi.staticsecurity.web.StaticSecurityWebConfiguration");
        } catch (ClassNotFoundException ignored) {
        }
        return result.toArray(String[]::new);
    }

}
