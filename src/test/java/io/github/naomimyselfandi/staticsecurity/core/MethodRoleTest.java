package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class MethodRoleTest {

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
        default Object helper() {
            return fail();
        }

        @Helper(Helper.Type.DIRECT)
        default Object helper(Object ignored) {
            return fail();
        }

        @Helper(Helper.Type.CACHED)
        default Object cached() {
            return fail();
        }

        @Helper(Helper.Type.CACHED)
        default Object cached(Object ignored) {
            return fail();
        }

        @Helper(Helper.Type.SPRING)
        Object spring();

        Object methodWithParameter(Object object);

        void voidMethod();

        @Helper(Helper.Type.DIRECT)
        Object directHelperWithoutDefault();

        @Helper(Helper.Type.CACHED)
        Object cachedHelperWithoutDefault();

        @Helper(Helper.Type.SPRING)
        default Object springHelperWithDefault() {
            return fail();
        }

        @Helper(Helper.Type.SPRING)
        Object springHelperWithParameters(Object object);

        @Helper(Helper.Type.SPRING)
        void voidSpringHelper();

        Object __auth__(Object object);

        Object __data__(Object object);

        default Object __auth__(String string) {
            return fail(string);
        }

        default Object __data__(String string) {
            return fail(string);
        }

    }

    @MethodSource
    @ParameterizedTest
    void getRole(Method method, MethodRole expected) {
        assertThat(MethodRole.of(method)).isEqualTo(expected);
    }

    @MethodSource
    @ParameterizedTest
    void getRole_WhenTheMethodHasNoRole_ThenThrows(Method method, String message) {
        assertThatThrownBy(() -> MethodRole.of(method))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(message, method);
    }

    private static Stream<Arguments> getRole() throws NoSuchMethodException {
        return Stream.of(
                arguments(Holder.class.getMethod("requiredProperty"), MethodRole.REQUIRED),
                arguments(Holder.class.getMethod("optionalProperty"), MethodRole.OPTIONAL),
                arguments(Holder.class.getMethod("optionalIntProperty"), MethodRole.OPTIONAL),
                arguments(Holder.class.getMethod("optionalLongProperty"), MethodRole.OPTIONAL),
                arguments(Holder.class.getMethod("optionalDoubleProperty"), MethodRole.OPTIONAL),
                arguments(Holder.class.getMethod("defaultProperty"), MethodRole.OPTIONAL),
                arguments(Object.class.getMethod("equals", Object.class), MethodRole.EQUALS),
                arguments(Object.class.getMethod("hashCode"), MethodRole.HASH_CODE),
                arguments(Object.class.getMethod("toString"), MethodRole.TO_STRING),
                arguments(Holder.class.getMethod("equals", Object.class), MethodRole.EQUALS),
                arguments(Holder.class.getMethod("hashCode"), MethodRole.HASH_CODE),
                arguments(Holder.class.getMethod("toString"), MethodRole.TO_STRING),
                arguments(Holder.class.getMethod("helper"), MethodRole.DIRECT_HELPER),
                arguments(Holder.class.getMethod("helper", Object.class), MethodRole.DIRECT_HELPER),
                arguments(Holder.class.getMethod("cached"), MethodRole.CACHED_HELPER),
                arguments(Holder.class.getMethod("cached", Object.class), MethodRole.CACHED_HELPER),
                arguments(Holder.class.getMethod("spring"), MethodRole.SPRING_HELPER),
                arguments(Clearance.class.getMethod("__auth__"), MethodRole.AUTH),
                arguments(Clearance.class.getMethod("__data__"), MethodRole.DATA)
        );
    }

    private static Stream<Arguments> getRole_WhenTheMethodHasNoRole_ThenThrows() throws NoSuchMethodException {
        return Stream.of(
                arguments(
                        Holder.class.getMethod("methodWithParameter", Object.class),
                        "Misconfigured clearance method %s."
                ),
                arguments(
                        Holder.class.getMethod("voidMethod"),
                        "Misconfigured clearance method %s."
                ),
                arguments(
                        Holder.class.getMethod("directHelperWithoutDefault"),
                        "Helper method %s should have a default implementation."
                ),
                arguments(
                        Holder.class.getMethod("cachedHelperWithoutDefault"),
                        "Helper method %s should have a default implementation."
                ),
                arguments(
                        Holder.class.getMethod("springHelperWithDefault"),
                        "Misconfigured Spring helper method %s."
                ),
                arguments(
                        Holder.class.getMethod("springHelperWithParameters", Object.class),
                        "Misconfigured Spring helper method %s."
                ),
                arguments(
                        Holder.class.getMethod("voidSpringHelper"),
                        "Misconfigured Spring helper method %s."
                ),
                arguments(
                        Holder.class.getMethod("__auth__", Object.class),
                        "Misconfigured clearance method %s."
                ),
                arguments(
                        Holder.class.getMethod("__data__", Object.class),
                        "Misconfigured clearance method %s."
                ),
                arguments(
                        Holder.class.getMethod("__auth__", String.class),
                        "Misconfigured clearance method %s."
                ),
                arguments(
                        Holder.class.getMethod("__data__", String.class),
                        "Misconfigured clearance method %s."
                )
        );
    }

}
