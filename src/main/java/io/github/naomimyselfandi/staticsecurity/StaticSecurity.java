package io.github.naomimyselfandi.staticsecurity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;

@RequiredArgsConstructor(staticName = "forTesting")
class StaticSecurity implements ImportSelector {

    private final ClassLoader classLoader;

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
        } catch (ClassNotFoundException ignored) {}
        return result.toArray(String[]::new);
    }

}
