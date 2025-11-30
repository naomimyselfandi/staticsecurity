package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.Deserializers;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StaticSecurityModuleTest {

    @Captor
    private ArgumentCaptor<Deserializers> captor;

    @Mock
    private Module.SetupContext setupContext;

    @Mock
    private StaticSecurityService staticSecurityService;

    @InjectMocks
    private StaticSecurityModule fixture;

    @Test
    void getModuleName() {
        assertThat(fixture.getModuleName()).isEqualTo("StaticSecurityModule");
    }

    @Test
    void version() {
        assertThat(fixture.version())
                .returns(1, Version::getMajorVersion)
                .returns(0, Version::getMinorVersion)
                .returns(0, Version::getPatchLevel)
                .returns(false, Version::isSnapshot)
                .returns("io.github.naomimyselfandi", Version::getGroupId)
                .returns("staticsecurity", Version::getArtifactId);
    }

    @Test
    void setupModule() {
        fixture.setupModule(setupContext);
        verify(setupContext).addDeserializers(captor.capture());
        assertThat(captor.getValue())
                .asInstanceOf(InstanceOfAssertFactories.type(ClearanceDeserializers.class))
                .returns(staticSecurityService, it -> it.staticSecurityService)
                .satisfies(it -> verify(setupContext).addKeyDeserializers(it));
    }

}
