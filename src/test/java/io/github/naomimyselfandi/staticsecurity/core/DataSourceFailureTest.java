package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.ClearanceSourceException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class DataSourceFailureTest {

    @Test
    void get() {
        var message = UUID.randomUUID().toString();
        assertThatThrownBy(new DataSource.Failure(message)::get)
                .isInstanceOf(ClearanceSourceException.class)
                .hasMessage(message);
    }

}
