package io.github.naomimyselfandi.staticsecurity.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSourcePairTest {

    @Mock
    private DataSource.Result success;

    @Mock
    private DataSource<Object> primary, secondary;

    private DataSourcePair<Object> fixture;

    @BeforeEach
    void setup() {
        fixture = new DataSourcePair<>(primary, secondary);
    }

    @Test
    void getData_WhenThePrimarySourceSucceeds_ThenReturnsItsResult() {
        var source = new Object();
        when(primary.getData(source)).thenReturn(success);
        assertThat(fixture.getData(source)).isEqualTo(success);
        verifyNoInteractions(secondary);
    }

    @Test
    void getData_WhenThePrimarySourceFails_ThenTriesTheSecondarySource() {
        var source = new Object();
        when(primary.getData(source)).thenReturn(new DataSource.Failure(UUID.randomUUID().toString()));
        when(secondary.getData(source)).thenReturn(success);
        assertThat(fixture.getData(source)).isEqualTo(success);
    }

    @Test
    void getData_WhenBothSourcesFail_ThenReturnsTheFirstFailure() {
        var source = new Object();
        var primaryResult = new DataSource.Failure(UUID.randomUUID().toString());
        var secondaryResult = new DataSource.Failure(UUID.randomUUID().toString());
        when(primary.getData(source)).thenReturn(primaryResult);
        when(secondary.getData(source)).thenReturn(secondaryResult);
        assertThat(fixture.getData(source)).isEqualTo(primaryResult);
    }

}
