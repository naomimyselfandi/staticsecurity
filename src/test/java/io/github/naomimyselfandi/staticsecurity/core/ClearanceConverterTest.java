package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.PendingClearance;
import io.github.naomimyselfandi.staticsecurity.StaticSecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.TypeDescriptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearanceConverterTest {

    private interface Source {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Source.class);
    }

    private interface Target extends Clearance {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Target.class);
    }

    @Mock
    private Source source;

    @Mock
    private PendingClearance<Target> request;

    @Mock
    private Target target;

    @Mock
    private StaticSecurityService staticSecurityService;

    @InjectMocks
    private ClearanceConverter fixture;

    @Test
    void getConvertibleTypes() {
        assertThat(fixture.getConvertibleTypes()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void matches(boolean expected) {
        when(staticSecurityService.canCreate(Source.class, Target.class)).thenReturn(expected);
        assertThat(fixture.matches(Source.TYPE, Target.TYPE)).isEqualTo(expected);
    }

    @Test
    void convert() {
        when(staticSecurityService.create(source, Target.class)).thenReturn(request);
        when(request.require()).thenReturn(target);
        assertThat(fixture.convert(source, Source.TYPE, Target.TYPE)).isEqualTo(target);
    }

    @Test
    void convert_WhenTheSourceIsNull_ThenNull() {
        assertThat(fixture.convert(null, Source.TYPE, Target.TYPE)).isNull();
        verifyNoInteractions(staticSecurityService);
    }

}
