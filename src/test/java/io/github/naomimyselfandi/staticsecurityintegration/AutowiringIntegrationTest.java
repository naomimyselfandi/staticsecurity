package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.ClearanceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.OptionalInt;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
class AutowiringIntegrationTest {

    @Autowired(required = false)
    private ClearanceFactory<UUID, DocumentRequest> literalFactory;

    @Autowired(required = false)
    private ClearanceFactory<String, DocumentRequest> convertingFactory;

    @Autowired(required = false)
    private ClearanceFactory<DocumentRequestDto, DocumentRequest> extractingFactory;

    @Autowired(required = false)
    private ClearanceFactory<DocumentUpdateRequestDto, DocumentUpdateRequest> multiExtractingFactory;

    @Test
    void literalFactory() {
        var id = UUID.randomUUID();
        assertThat(literalFactory.create(id).require()).returns(id, DocumentRequest::getId);
    }

    @Test
    void requiredFactory() {
        var id = UUID.randomUUID();
        assertThat(convertingFactory.create(id.toString()).require()).returns(id, DocumentRequest::getId);
    }

    @Test
    void extractingFactory() {
        var id = UUID.randomUUID();
        var dto = new DocumentRequestDto(id.toString());
        assertThat(extractingFactory.create(dto).require()).returns(id, DocumentRequest::getId);
    }

    @Test
    void multiExtractingFactory() {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var dto = new DocumentUpdateRequestDto(id.toString(), contents);
        assertThat(multiExtractingFactory.create(dto).require())
                .returns(id, DocumentRequest::getId)
                .returns(contents, DocumentUpdateRequest::getContents)
                .returns(OptionalInt.empty(), DocumentUpdateRequest::getChapter)
                .returns(true, DocumentUpdateRequest::createsNewChapter);
    }

}
