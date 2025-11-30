package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.core.Version;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class StaticSecurityModule extends com.fasterxml.jackson.databind.Module {

    private final StaticSecurityService staticSecurityService;

    @Override
    public String getModuleName() {
        return "StaticSecurityModule";
    }

    @Override
    public Version version() {
        var groupId = "io.github.naomimyselfandi";
        var artifactId = "staticsecurity";
        return new Version(1, 0,0, null, groupId, artifactId);
    }

    @Override
    public void setupModule(SetupContext context) {
        var deserializers = new ClearanceDeserializers(staticSecurityService);
        context.addDeserializers(deserializers);
        context.addKeyDeserializers(deserializers);
    }

}
