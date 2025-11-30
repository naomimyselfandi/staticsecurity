package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
class ConversionIntegrationTest {

    @Autowired
    private StaticSecurityService staticSecurityService;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private AccessPolicy<DocumentRequest> accessPolicy;

    private enum SourceType {DTO, STRING, UUID}

    @BeforeEach
    void setup() {
        lenient().when(accessPolicy.check(any())).thenReturn(null);
    }

    @EnumSource
    @ParameterizedTest
    void convert(SourceType sourceType) {
        var id = UUID.randomUUID();
        var source = switch (sourceType) {
            case DTO -> new DocumentRequestDto(id.toString());
            case STRING -> id.toString();
            case UUID -> id;
        };
        assertThat(conversionService.convert(source, DocumentRequest.class)).returns(id, DocumentRequest::getId);
    }

    @Test
    void convert() {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var source = new DocumentUpdateRequestDto(id.toString(), contents);
        assertThat(conversionService.convert(source, DocumentUpdateRequest.class))
                .returns(id, DocumentRequest::getId)
                .returns(contents, DocumentUpdateRequest::getContents)
                .returns(Optional.empty(), DocumentUpdateRequest::getChapter)
                .returns(true, DocumentUpdateRequest::createsNewChapter);
    }

    @EnumSource
    @ParameterizedTest
    void convert_WhenAccessIsDenied_ThenThrows(SourceType sourceType) {
        var id = UUID.randomUUID();
        var source = switch (sourceType) {
            case DTO -> new DocumentRequestDto(id.toString());
            case STRING -> id.toString();
            case UUID -> id;
        };
        var e = new RuntimeException();
        when(accessPolicy.check(any())).thenReturn(() -> e);
        assertThatThrownBy(() -> conversionService.convert(source, DocumentRequest.class)).hasCause(e);
    }

    @Test
    void convert_WhenAccessIsDenied_ThenThrows() {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var source = new DocumentUpdateRequestDto(id.toString(), contents);
        var e = new RuntimeException();
        when(accessPolicy.check(any())).thenReturn(() -> e);
        assertThatThrownBy(() -> conversionService.convert(source, DocumentUpdateRequest.class)).hasCause(e);
    }

    @Test
    void canConvertBack() {
        var id = UUID.randomUUID();
        var clearance = staticSecurityService.create(id, DocumentRequest.class).require();
        assertThat(conversionService.convert(clearance, UUID.class)).isEqualTo(id);
        assertThat(conversionService.convert(clearance, String.class)).isEqualTo(id.toString());
    }

    @Test
    void reverseConversionOnlyAppliesToSimpleClearances() {
        assertThat(conversionService.canConvert(DocumentRequest.class, UUID.class)).isTrue();
        assertThat(conversionService.canConvert(DocumentRequest.class, String.class)).isTrue();
        assertThat(conversionService.canConvert(DocumentUpdateRequest.class, UUID.class)).isFalse();
        assertThat(conversionService.canConvert(DocumentUpdateRequest.class, String.class)).isFalse();
    }

}
