package io.github.naomimyselfandi.staticsecurityintegration;

import io.github.naomimyselfandi.staticsecurity.AccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfigurationWithMockMvc.class)
class WebIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccessPolicy<DocumentRequest> accessPolicy;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).alwaysDo(print()).build();
        when(accessPolicy.check(any())).thenReturn(null);
    }

    @Test
    void canMergeClearances() throws Exception {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var expected = "DocumentUpdateRequest(contents=%s, id=%s)".formatted(contents, id);
        var actual = mockMvc
                .perform(patch("/test/ing/foo/{id}", id).queryParam("contents", contents))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canOverrideDefaults(boolean createsNewChapter) throws Exception {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var chapter = ThreadLocalRandom.current().nextInt(1, 10);
        var expected = "DocumentUpdateRequest(chapter=Optional[%d], contents=%s, createsNewChapter=%s, id=%s)"
                .formatted(chapter, contents, createsNewChapter, id);
        var actual = mockMvc
                .perform(patch("/test/ing/foo/{id}", id)
                        .queryParam("contents", contents)
                        .queryParam("new", String.valueOf(createsNewChapter))
                        .queryParam("chapter", String.valueOf(chapter)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mergedClearancesImplyAccessChecks() throws Exception {
        var status = HttpStatus.valueOf(ThreadLocalRandom.current().nextInt(400, 420));
        when(accessPolicy.check(any())).thenReturn(() -> new ResponseStatusException(status));
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        mockMvc.perform(patch("/test/ing/foo/{id}", id).queryParam("contents", contents))
                .andExpect(status().is(status.value()));
    }

    @Test
    void canUnwrapBodyWhileMergingClearances() throws Exception {
        var id = UUID.randomUUID();
        var contents = UUID.randomUUID().toString();
        var chapter = ThreadLocalRandom.current().nextInt(1, 10);
        var expected = "DocumentUpdateRequest(chapter=Optional[%d], contents=%s, id=%s)"
                .formatted(chapter, contents, id);
        var actual = mockMvc
                .perform(patch("/test/ing/bar/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "contents": "%s",
                          "chapter": %d
                        }
                        """.formatted(contents, chapter)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(actual).isEqualTo(expected);
    }

}
