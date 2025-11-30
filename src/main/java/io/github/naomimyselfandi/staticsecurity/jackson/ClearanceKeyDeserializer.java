package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
final class ClearanceKeyDeserializer<C extends Clearance> extends KeyDeserializer {

    final Class<C> clearanceType;
    final StaticSecurityService staticSecurityService;

    @Override
    public Object deserializeKey(String key, @Nullable DeserializationContext context) {
        return staticSecurityService.create(key, clearanceType).require();
    }

}

