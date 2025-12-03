package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;

class PropertyCacheTest {

    private PropertyCache fixture;

    @BeforeEach
    void setup() {
        fixture = new PropertyCache();
    }

    @Test
    void calculate() throws NoSuchMethodException {
        interface Something {}
        interface TestClearance extends Clearance {
            boolean equals(@Nullable Object other);
            int hashCode();
            String toString();
            TestClearance clone();
            Something foo();
            Something getFoo();
            boolean isFoo();
            Something bar();
            Something getBar();
            boolean isBar();
            Something getup();
            boolean island();
            Optional<Object> getOptional();
            OptionalInt getOptionalInt();
            OptionalLong getOptionalLong();
            OptionalDouble getOptionalDouble();
            default Something getDefault() {
                return fail();
            }
            @SuppressWarnings("unused") Something notProperty(Object parameter);
            @SuppressWarnings("unused") void notProperty();
            @SuppressWarnings("unused") @Helper(Helper.Type.DIRECT) Object directHelper();
            @SuppressWarnings("unused") @Helper(Helper.Type.CACHED) Object cachedHelper();
            @SuppressWarnings("unused") @Helper(Helper.Type.SPRING) Object springHelper();
        }
        var something = TypeDescriptor.valueOf(Something.class);
        var optObj = new TypeDescriptor(
                ResolvableType.forMethodReturnType(TestClearance.class.getMethod("getOptional")),
                null,
                null);
        var optInt = TypeDescriptor.valueOf(OptionalInt.class);
        var optLng = TypeDescriptor.valueOf(OptionalLong.class);
        var optDbl = TypeDescriptor.valueOf(OptionalDouble.class);
        var bool = TypeDescriptor.valueOf(boolean.class);
        var type = TestClearance.class;
        var req = true;
        var opt = false;
        assertThat(fixture.calculate(type)).containsExactly(
                new PropertyImpl("bar", something, type.getMethod("bar"), req),
                new PropertyImpl("foo", something, type.getMethod("foo"), req),
                new PropertyImpl("bar", something, type.getMethod("getBar"), req),
                new PropertyImpl("default", something, type.getMethod("getDefault"), false),
                new PropertyImpl("foo", something, type.getMethod("getFoo"), req),
                new PropertyImpl("optional", optObj, type.getMethod("getOptional"), opt),
                new PropertyImpl("optionalDouble", optDbl, type.getMethod("getOptionalDouble"), opt),
                new PropertyImpl("optionalInt", optInt, type.getMethod("getOptionalInt"), opt),
                new PropertyImpl("optionalLong", optLng, type.getMethod("getOptionalLong"), opt),
                new PropertyImpl("getup", something, type.getMethod("getup"), req),
                new PropertyImpl("bar", bool, type.getMethod("isBar"), req),
                new PropertyImpl("foo", bool, type.getMethod("isFoo"), req),
                new PropertyImpl("island", bool, type.getMethod("island"), req)
        );
    }

}
