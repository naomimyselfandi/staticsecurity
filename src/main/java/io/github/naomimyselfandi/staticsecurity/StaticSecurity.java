package io.github.naomimyselfandi.staticsecurity;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

class StaticSecurity implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{"io.github.naomimyselfandi.staticsecurity.core.StaticSecurityConfiguration"};
    }

}
