package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.springframework.lang.Nullable;

import java.io.IOException;

final class ClearanceDeserializer<C extends Clearance> extends StdDeserializer<C> {

    final Class<C> clearanceType;
    final StaticSecurityService staticSecurityService;

    ClearanceDeserializer(Class<C> clearanceType, StaticSecurityService staticSecurityService) {
        super(clearanceType);
        this.clearanceType = clearanceType;
        this.staticSecurityService = staticSecurityService;
    }

    @Override
    public C deserialize(JsonParser parser, @Nullable DeserializationContext context) throws IOException {
        return staticSecurityService.create(parser.readValueAsTree(), clearanceType).require();
    }

}
