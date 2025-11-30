package io.github.naomimyselfandi.staticsecurity.core;

import io.github.naomimyselfandi.staticsecurity.Clearance;
import io.github.naomimyselfandi.staticsecurity.MethodInfo;
import io.github.naomimyselfandi.staticsecurity.PropertyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSourceCacheTest {

    private interface Source {}

    private interface Foo {}
    private interface Bar {}
    private interface Baz {}

    private interface TestClearance extends Clearance {

        Foo getFoo();

        Bar getBar();

        default Baz getBaz() {
            return fail();
        }

    }

    private interface TestSimpleClearance extends TestClearance {

        @Override
        default Bar getBar() {
            return fail();
        }

    }

    @Mock
    private PropertyProvider<Source> provider;

    @Mock
    private Cache<Class<?>, PropertyProvider<?>> propertyProviderCache;

    @InjectMocks
    private DataSourceCache fixture;

    @BeforeEach
    void setup() {
        when((Object) propertyProviderCache.get(Source.class)).thenReturn(provider);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calculate_Extract(boolean optionalPropertyIsAvailable) {
        when(provider.canExtract(any())).then(invocation -> {
            var method = invocation.<Method>getArgument(0);
            return method.getReturnType() != Baz.class || optionalPropertyIsAvailable;
        });
        lenient().when(provider.canFlatten(any())).thenReturn(true); // not used b/c multiple required properties
        var expected = new ExtractingDataSource<>(provider, MethodInfo.getProperties(TestClearance.class));
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class)))
                .map(Function.<Object>identity())
                .contains(expected);
    }

    @RepeatedTest(2)
    void calculate_Extract_WhenARequiredPropertyIsUnavailable_ThenReturnsNothing(RepetitionInfo repetitionInfo) {
        when(provider.canExtract(any())).then(invocation -> {
            var method = invocation.<Method>getArgument(0);
            if (method.getReturnType() == Foo.class) {
                return repetitionInfo.getCurrentRepetition() == 1;
            } else if (method.getReturnType() == Bar.class) {
                return repetitionInfo.getCurrentRepetition() == 2;
            } else {
                return true;
            }
        });
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestClearance.class))).isEmpty();
    }

    @RepeatedTest(4)
    void calculate_Flatten(RepetitionInfo repetitionInfo) throws NoSuchMethodException {
        when(provider.canFlatten(any())).then(invocation -> {
            var method = invocation.<Method>getArgument(0);
            if (method.getReturnType() == Bar.class) {
                return ((repetitionInfo.getCurrentRepetition() - 1) & 1) == 1;
            } else if (method.getReturnType() == Baz.class) {
                return ((repetitionInfo.getCurrentRepetition() - 1) & 2) == 2;
            } else {
                return true;
            }
        });
        var property = TestSimpleClearance.class.getMethod("getFoo");
        var expected = new FlatteningDataSource<>(provider, property);
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestSimpleClearance.class)))
                .map(Function.<Object>identity())
                .contains(expected);
    }

    @Test
    void calculate_Flatten_WhenThePropertyIsUnavailable_ThenDoesNotCreateAnExtractor() {
        when(provider.canFlatten(any())).then(invocation -> {
            var method = invocation.<Method>getArgument(0);
            return method.getReturnType() != Foo.class;
        });
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestSimpleClearance.class))).isEmpty();
    }

    @Test
    void calculate_Pair() throws NoSuchMethodException {
        when(provider.canExtract(any())).thenReturn(true);
        when(provider.canFlatten(any())).thenReturn(true);
        var expected = new DataSourcePair<>(
                new ExtractingDataSource<>(provider, MethodInfo.getProperties(TestSimpleClearance.class)),
                new FlatteningDataSource<>(provider, TestSimpleClearance.class.getMethod("getFoo"))
        );
        assertThat(fixture.calculate(new DataSourceKey(Source.class, TestSimpleClearance.class)))
                .map(Function.<Object>identity())
                .contains(expected);
    }

}
