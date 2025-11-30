package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
final class ClearanceDeserializers extends Deserializers.Base implements KeyDeserializers {

    final StaticSecurityService staticSecurityService;

    @Override
    public @Nullable JsonDeserializer<?> findBeanDeserializer(
            JavaType javaType,
            @Nullable DeserializationConfig config,
            @Nullable BeanDescription beanDesc
    ) {
        var type = javaType.getRawClass();
        if (staticSecurityService.canCreate(JsonNode.class, type)) {
            return new ClearanceDeserializer<>(type.asSubclass(Clearance.class), staticSecurityService);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable KeyDeserializer findKeyDeserializer(
            JavaType javaType,
            @Nullable DeserializationConfig config,
            @Nullable BeanDescription beanDesc
    ) {
        var type = javaType.getRawClass();
        if (staticSecurityService.canCreate(String.class, type)) {
            return new ClearanceKeyDeserializer<>(type.asSubclass(Clearance.class), staticSecurityService);
        } else {
            return null;
        }
    }

}
