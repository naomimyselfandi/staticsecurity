package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PropertyProviderTest {

    private static class TestProvider<S> implements PropertyProvider<S> {

        @Override
        public @Nullable Object extract(@NotNull S source, @NotNull Property property) {
            return fail();
        }

        @Override
        public @Nullable Object flatten(@NotNull S source, @NotNull Property property) {
            return fail();
        }

    }

    @Mock
    private Property property;

    @Test
    void canExtract() {
        assertThat(new TestProvider<>().canExtract(property)).isTrue();
    }

    @Test
    void canFlatten() {
        assertThat(new TestProvider<>().canFlatten(property)).isTrue();
    }

    @Test
    void getSourceType() {
        interface Something {}
        TestProvider<Something> provider = new TestProvider<>() {};
        assertThat(provider.getSourceType()).isEqualTo(Something.class);
    }

    @Test
    void getSourceType_WhenTypeInformationIsUnavailable_ThenThrows() {
        interface Something {}
        var provider = new TestProvider<Something>();
        var fmt = "Could not determine type information for %s. Consider overriding getSourceType().";
        assertThatThrownBy(provider::getSourceType)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(fmt, provider.getClass().getName());
    }

}
