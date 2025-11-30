package io.github.naomimyselfandi.staticsecurity.core;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

class CacheTest {

    @Test
    void get() {
        var values = new HashMap<>();
        var a = new Object();
        var b = new Object();
        var c = new Object();
        var d = new Object();
        values.put(a, b);
        values.put(c, d);
        var cache = new Cache<>() {

            @Override
            @NotNull Object calculate(@NotNull Object input) {
                return Objects.requireNonNull(values.remove(input));
            }

        };
        assertThat(cache.get(a)).isEqualTo(b);
        assertThat(cache.get(a)).isEqualTo(b);
        assertThat(cache.get(c)).isEqualTo(d);
        assertThat(cache.get(c)).isEqualTo(d);
    }

}
