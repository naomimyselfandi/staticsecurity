package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.util.*;

record ClearanceConverter(StaticSecurityService staticSecurityService) implements ConditionalGenericConverter {

    @Override
    public @Nullable Set<ConvertiblePair> getConvertibleTypes() {
        return null; // we can't determine these pairs in advance, so rely on matches() alone
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return staticSecurityService.canCreate(sourceType.getObjectType(), targetType.getType());
    }

    @Override
    public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source != null) {
            return staticSecurityService.create(source, targetType.getType().asSubclass(Clearance.class)).require();
        } else {
            return null;
        }
    }

}
