package io.github.naomimyselfandi.staticsecurityintegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.OptionalInt;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfigurationWithJackson.class)
class JacksonIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccessPolicy<DocumentRequest> accessPolicy;

    private enum SourceType {DTO, STRING, UUID}

    @EnumSource
    @ParameterizedTest
    void convertValue(SourceType sourceType) {
        var id = UUID.randomUUID();
        var source = switch (sourceType) {
            case DTO -> new DocumentRequestDto(id.toString());
            case STRING -> id.toString();
            case UUID -> id;
        };
        when(accessPolicy.check(any())).thenReturn(null);
        assertThat(objectMapper.convertValue(source, DocumentRequest.class)).returns(id, DocumentRequest::getId);
    }

    @Test
    void convertValue() {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var source = new DocumentUpdateRequestDto(id.toString(), contents);
        when(accessPolicy.check(any())).thenReturn(null);
        assertThat(objectMapper.convertValue(source, DocumentUpdateRequest.class))
                .returns(id, DocumentRequest::getId)
                .returns(contents, DocumentUpdateRequest::getContents)
                .returns(OptionalInt.empty(), DocumentUpdateRequest::getChapter)
                .returns(true, DocumentUpdateRequest::createsNewChapter);
    }

    @EnumSource
    @ParameterizedTest
    void convertValue_WhenAccessIsDenied_ThenThrows(SourceType sourceType) {
        var id = UUID.randomUUID();
        var source = switch (sourceType) {
            case DTO -> new DocumentRequestDto(id.toString());
            case STRING -> id.toString();
            case UUID -> id;
        };
        var e = new RuntimeException();
        when(accessPolicy.check(any())).thenReturn(() -> e);
        assertThatThrownBy(() -> objectMapper.convertValue(source, DocumentRequest.class)).isEqualTo(e);
    }

    @Test
    void convertValue_WhenAccessIsDenied_ThenThrows() {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var source = new DocumentUpdateRequestDto(id.toString(), contents);
        var e = new RuntimeException();
        when(accessPolicy.check(any())).thenReturn(() -> e);
        assertThatThrownBy(() -> objectMapper.convertValue(source, DocumentUpdateRequest.class)).isEqualTo(e);
    }

}
