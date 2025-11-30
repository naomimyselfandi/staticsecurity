package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearanceDeserializersTest {

    private interface TestClearance extends Clearance {}

    @Mock
    private StaticSecurityService staticSecurityService;

    @InjectMocks
    private ClearanceDeserializers fixture;

    @Test
    void findBeanDeserializer() {
        when(staticSecurityService.canCreate(JsonNode.class, TestClearance.class)).thenReturn(true);
        var javaType = TypeFactory.defaultInstance().constructType(TestClearance.class);
        assertThat(fixture.findBeanDeserializer(javaType, null, null))
                .asInstanceOf(InstanceOfAssertFactories.type(ClearanceDeserializer.class))
                .returns(TestClearance.class, it -> it.clearanceType)
                .returns(staticSecurityService, it -> it.staticSecurityService);
    }

    @Test
    void findBeanDeserializer_WhenTheTypeIsNotAppropriate_ThenNull() {
        when(staticSecurityService.canCreate(JsonNode.class, TestClearance.class)).thenReturn(false);
        var javaType = TypeFactory.defaultInstance().constructType(TestClearance.class);
        assertThat(fixture.findBeanDeserializer(javaType, null, null)).isNull();
    }

    @Test
    void findKeyDeserializer() {
        when(staticSecurityService.canCreate(String.class, TestClearance.class)).thenReturn(true);
        var javaType = TypeFactory.defaultInstance().constructType(TestClearance.class);
        assertThat(fixture.findKeyDeserializer(javaType, null, null))
                .asInstanceOf(InstanceOfAssertFactories.type(ClearanceKeyDeserializer.class))
                .returns(staticSecurityService, it -> it.staticSecurityService);
    }

    @Test
    void findKeyDeserializer_WhenTheTypeIsNotAppropriate_ThenNull() {
        when(staticSecurityService.canCreate(String.class, TestClearance.class)).thenReturn(false);
        var javaType = TypeFactory.defaultInstance().constructType(TestClearance.class);
        assertThat(fixture.findKeyDeserializer(javaType, null, null)).isNull();
    }

}
