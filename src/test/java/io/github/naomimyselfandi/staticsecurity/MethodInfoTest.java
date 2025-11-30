package io.github.naomimyselfandi.staticsecurity;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class MethodInfoTest {

    private interface Holder extends Clearance {

        @Override
        boolean equals(@Nullable Object other);

        @Override
        int hashCode();

        @Override
        String toString();

        Object requiredProperty();

        Optional<Object> optionalProperty();

        OptionalInt optionalIntProperty();

        OptionalLong optionalLongProperty();

        OptionalDouble optionalDoubleProperty();

        default Object defaultProperty() {
            return fail();
        }

        @Helper(Helper.Type.DIRECT)
        default Object directHelper() {
            return fail();
        }

        @Helper(Helper.Type.DIRECT)
        default Object directHelper(Object ignored) {
            return fail();
        }

        @Helper(Helper.Type.CACHED)
        default Object cachedHelper() {
            return fail();
        }

        @Helper(Helper.Type.CACHED)
        default Object cachedHelper(Object ignored) {
            return fail();
        }

        @Helper(Helper.Type.SPRING)
        Object springHelper();

        Object somethingElse(Object parameter);

    }

    @MethodSource
    @ParameterizedTest
    void getName(Method method, String expected) {
        assertThat(MethodInfo.getName(method)).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unused")
    void getType() {
        interface Foo<T> {}
        interface Bar {}
        interface Holder {
            Foo<Bar> foobar();
        }
        var method = Holder.class.getMethods()[0];
        assertThat(MethodInfo.getType(method))
                .returns(Foo.class, TypeDescriptor::getType)
                .extracting(it -> it.nested(1))
                .isNotNull()
                .returns(Bar.class, TypeDescriptor::getType);
    }

    @Test
    @SuppressWarnings("unused")
    void getResolvableType() {
        interface Foo<T> {}
        interface Bar {}
        interface Holder {
            Foo<Bar> foobar();
        }
        var method = Holder.class.getMethods()[0];
        assertThat(MethodInfo.getResolvableType(method))
                .returns(Foo.class, ResolvableType::toClass)
                .extracting(ResolvableType::getGeneric)
                .returns(Bar.class, ResolvableType::toClass);
    }

    @MethodSource
    @ParameterizedTest
    void isProperty(Method method, boolean expected) {
        assertThat(MethodInfo.isProperty(method)).isEqualTo(expected);
    }

    @Test
    void getProperties() throws NoSuchMethodException {
        assertThat(MethodInfo.getProperties(Holder.class)).containsExactly(
                Holder.class.getMethod("defaultProperty"),
                Holder.class.getMethod("optionalDoubleProperty"),
                Holder.class.getMethod("optionalIntProperty"),
                Holder.class.getMethod("optionalLongProperty"),
                Holder.class.getMethod("optionalProperty"),
                Holder.class.getMethod("requiredProperty")
        );
    }

    private static Stream<Arguments> isProperty() throws NoSuchMethodException {
        return Stream.of(
                arguments(Holder.class.getMethod("requiredProperty"), true),
                arguments(Holder.class.getMethod("optionalProperty"), true),
                arguments(Holder.class.getMethod("optionalIntProperty"), true),
                arguments(Holder.class.getMethod("optionalLongProperty"), true),
                arguments(Holder.class.getMethod("optionalDoubleProperty"), true),
                arguments(Holder.class.getMethod("defaultProperty"), true),
                arguments(Object.class.getMethod("equals", Object.class), false),
                arguments(Object.class.getMethod("hashCode"), false),
                arguments(Object.class.getMethod("toString"), false),
                arguments(Holder.class.getMethod("equals", Object.class), false),
                arguments(Holder.class.getMethod("hashCode"), false),
                arguments(Holder.class.getMethod("toString"), false),
                arguments(Holder.class.getMethod("directHelper"), false),
                arguments(Holder.class.getMethod("directHelper", Object.class), false),
                arguments(Holder.class.getMethod("cachedHelper"), false),
                arguments(Holder.class.getMethod("cachedHelper", Object.class), false),
                arguments(Holder.class.getMethod("springHelper"), false),
                arguments(Clearance.class.getMethod("__auth__"), false),
                arguments(Clearance.class.getMethod("__data__"), false),
                arguments(Holder.class.getMethod("somethingElse", Object.class), false)
        );
    }

    private static Stream<Arguments> getName() throws NoSuchMethodException {
        interface Holder {

            Object foo();

            Object getFoo();

            boolean isFoo();

            Object bar();

            Object getBar();

            boolean isBar();

            Object get();

            boolean is();

            Object getup();

            boolean island();

        }
        return Stream.of(
                arguments(Holder.class.getMethod("foo"), "foo"),
                arguments(Holder.class.getMethod("getFoo"), "foo"),
                arguments(Holder.class.getMethod("isFoo"), "foo"),
                arguments(Holder.class.getMethod("bar"), "bar"),
                arguments(Holder.class.getMethod("getBar"), "bar"),
                arguments(Holder.class.getMethod("isBar"), "bar"),
                arguments(Holder.class.getMethod("get"), "get"),
                arguments(Holder.class.getMethod("is"), "is"),
                arguments(Holder.class.getMethod("getup"), "getup"),
                arguments(Holder.class.getMethod("island"), "island")
        );
    }

}
