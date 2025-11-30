package io.github.naomimyselfandi.staticsecurity.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.PendingClearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearanceDeserializerTest {

    private interface TestClearance extends Clearance {}

    @Mock
    private PendingClearance<TestClearance> pendingClearance;

    @Mock
    private TestClearance clearance;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private StaticSecurityService staticSecurityService;

    private ClearanceDeserializer<TestClearance> fixture;

    @BeforeEach
    void setup() {
        fixture = new ClearanceDeserializer<>(TestClearance.class, staticSecurityService);
    }

    @Test
    void deserialize() throws IOException {
        var node = new ObjectNode(JsonNodeFactory.instance, Map.of(
                UUID.randomUUID().toString(), new TextNode(UUID.randomUUID().toString()),
                UUID.randomUUID().toString(), new TextNode(UUID.randomUUID().toString())
        ));
        when(staticSecurityService.create(node, TestClearance.class)).thenReturn(pendingClearance);
        when(pendingClearance.require()).thenReturn(clearance);
        when(jsonParser.readValueAsTree()).thenReturn(node);
        assertThat(fixture.deserialize(jsonParser, null)).isEqualTo(clearance);
    }

}
